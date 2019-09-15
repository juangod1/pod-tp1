package grupo2.server.election;

import grupo2.api.model.*;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

// Aca va toda la sincronización
// Cada servicio se registra como listener aca para ciertos eventos y tambien notifica de
// los eventos de los cuales es responsable.
public class ElectionManager {

    private static final int NUMBER_OF_PROVINCIAL_WINNERS = 5;
    private final List<VoteObserver> observers = new ArrayList<>();
    private final List<Vote> votes = new ArrayList<>();
    private final Lock readLock, writeLock;
    private ElectionStatus electionStatus;

    // Cache results once election is finished
    private ElectionResults finalNationalResults = null;
    private final Map<Province, ElectionResults> finalProvincialResults = new EnumMap<>(Province.class);
    private final Map<Integer, ElectionResults> finalTableResults = new HashMap<>();



    public ElectionManager() {
        ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
        readLock = rwLock.readLock();
        writeLock = rwLock.writeLock();
        electionStatus = ElectionStatus.NOT_STARTED;
    }

    public void register(VoteObserver observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    private boolean notifyVote(Vote vote) {
        observers.forEach(vo -> vo.newVote(vote)); //TODO: Tirar threads. Quizas overkill?
        return true;
    }

    public ElectionResults getNationalResults() throws ElectionStateException {
        readLock.lock();
        ElectionResults results = null;
        switch (electionStatus) {
            case NOT_STARTED:
                readLock.unlock();
                throw new ElectionStateException("Election has not started!");
            case STARTED: results = firstPastThePost();
                break;
            case FINISHED: results = getFinalNationalResults();
                break;
        }
        readLock.unlock();
        return results;
    }

    public ElectionResults getProvincialResults(Province p) throws ElectionStateException {
        readLock.lock();
        ElectionResults results = null;
        switch (electionStatus) {
            case NOT_STARTED:
                readLock.unlock();
                throw new ElectionStateException("Election has not started!");
            case STARTED: results = firstPastThePost(v -> v.getProvince() == p);
                break;
            case FINISHED: results = getFinalProvincialResults(p);
                break;
        }
        readLock.unlock();
        return results;
    }

    private ElectionResults getFinalProvincialResults(Province p) {
        // No interesan mucho las race conditions aca, de ultima un par harán el trabajo duplicado.
        // Preferimos eso a bloquear cosas que podrian suceder en paralelo porque no hay peligro de inconsistencias
        if (finalProvincialResults.containsKey(p)) {
            return finalProvincialResults.get(p);
        }

        List<Vote> provinceVotes = votes.stream().filter(v -> v.getProvince() == p).collect(toList());
        SingleTransferableVoteCalculator calculator = new SingleTransferableVoteCalculator(provinceVotes, NUMBER_OF_PROVINCIAL_WINNERS);
        ElectionResults result = new ElectionResults(calculator.calculate(), electionStatus);

        finalProvincialResults.put(p, result);

        return result;
    }

    // Este metodo si es synchronized -- solo el primero que lo pide deberia hacer el procesamiento, y
    // que el resto esperen. Cuando el procesamiento ya esta hecho, lo unico que hace este metodo es una comparacion
    // por null y un return y importa perder paralelismo en eso que es tan corto.
    private synchronized ElectionResults getFinalNationalResults(){
        if(finalNationalResults == null) {
            finalNationalResults = new ElectionResults(new AlternativeVoteCalculator(votes).calculate(), electionStatus);
        }
        return finalNationalResults;
    }


    public ElectionResults getTableResults(int table) throws ElectionStateException {
        ElectionResults results = null;
        readLock.lock();
        switch (electionStatus) {
            case NOT_STARTED:
                readLock.unlock();
                throw new ElectionStateException("Election has not started!");

            case STARTED:
                results =  firstPastThePost(v -> v.getBallotBox() == table);
                break;

            case FINISHED:
                results = getFinalTableVote(table);
                break;
        }
        readLock.unlock();
        return results;
    }

    private ElectionResults getFinalTableVote(int table) {
        // No interesan mucho las race conditions aca, de ultima un par harán el trabajo duplicado.
        // Preferimos eso a bloquear cosas que podrian suceder en paralelo porque no hay peligro de inconsistencias
        if (!finalTableResults.containsKey(table)) {
                finalTableResults.put(table, firstPastThePost(v -> v.getBallotBox() == table));
        }

        return finalTableResults.get(table);
    }

    private ElectionResults firstPastThePost(Predicate<Vote> filter)
    {
        List<Vote> filteredVotes = votes.stream().filter(filter).collect(toList());
        Map<Party, Double> results = new FirstPastThePostVoteCalculator(filteredVotes).calculate();
        return new ElectionResults(results, electionStatus);
    }

    private ElectionResults firstPastThePost()
    {
        Map<Party, Double> results = new FirstPastThePostVoteCalculator(votes).calculate();
        return new ElectionResults(results, electionStatus);
    }


    public ElectionStatus getElectionStatus() {
        return electionStatus;
    }

    public void setElectionStatus(ElectionStatus electionStatus) throws ElectionStateException {
        // Usar el mismo writeLock que para la lista de votos asegura un par de cosas
        //   1. Si alguien esta consultando la lista de votos para computar resultados parciales
        //      entonces no se le "cierra" la eleccion en el medio
        //   2. Si hay votos todavia pendientes de emision, van a estar en la cola de este lock
        //      y van a entrar antes de que cierre la eleccioon
        writeLock.lock();
        if(this.electionStatus==ElectionStatus.NOT_STARTED && electionStatus==ElectionStatus.STARTED ||
            this.electionStatus==ElectionStatus.STARTED && electionStatus==ElectionStatus.FINISHED){

            this.electionStatus = electionStatus;
            writeLock.unlock();
        }
        else{
            writeLock.unlock();
            throw new ElectionStateException(String.format("Illegal election state change. from %s to %s",this.electionStatus,electionStatus));
        }
    }

    public void addVote(Vote vote) throws ElectionStateException {
        writeLock.lock();
        switch(electionStatus) {
            case NOT_STARTED:
                writeLock.unlock();
                throw new ElectionStateException("Can't register vote. Election hasn't begun!");
            case STARTED:
                votes.add(vote);
                notifyVote(vote);
                break;
            case FINISHED:
                writeLock.unlock();
                throw new ElectionStateException("Can't register vote. Election has already finished");
        }
        writeLock.unlock();
    }
}
