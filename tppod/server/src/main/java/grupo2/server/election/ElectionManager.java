package grupo2.server.election;

import grupo2.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(ElectionManager.class);
    private static final int NUMBER_OF_PROVINCIAL_WINNERS = 5;
    private final List<VoteObserver> observers = new ArrayList<>();
    private final List<Vote> votes = new ArrayList<>();
    private final Lock readLock, writeLock;
    private ElectionStatus electionStatus;

    // Cache results once election is finished
    private ElectionResults finalNationalResults = null;
    private final Map<Province, ElectionResults> finalProvincialResults = new EnumMap<>(Province.class);

    private Object tableStatsLock = new Object();
    private int maxTable = Integer.MIN_VALUE;
    private int minTable = Integer.MAX_VALUE;
    private ElectionResults[] finalTableResults;
    private Object[] finalTableLocks;



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
        observers.forEach(vo -> vo.newVote(vote));
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
        synchronized (p) {      // Exclusion mutua para cada provincia
            if (finalProvincialResults.containsKey(p)) {
                return finalProvincialResults.get(p);
            }

            List<Vote> provinceVotes = votes.stream().filter(v -> v.getProvince() == p).collect(toList());
            SingleTransferableVoteCalculator calculator = new SingleTransferableVoteCalculator(provinceVotes, NUMBER_OF_PROVINCIAL_WINNERS);
            ElectionResults result = new ElectionResults(calculator.calculate(), electionStatus);

            finalProvincialResults.put(p, result);

            return result;
        }
    }

    // Solo el primero que lo pide deberia hacer el procesamiento, y
    // que el resto esperen.
    // Cuando el procesamiento ya esta hecho, lo unico que hace este metodo es una comparacion
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
        LOGGER.debug("Consulted table {}", table);
        return results;
    }

    private ElectionResults getFinalTableVote(int table) {
        if(table < minTable || table > maxTable) {
            return new ElectionResults(new HashMap<>(), electionStatus); // Empty
        }
        int index = table - minTable;
        synchronized (finalTableLocks[index]){
            LOGGER.debug("Locked table {}", index);
            if (finalTableResults[index] == null) {
                LOGGER.debug("Calculating table {}", index);
                finalTableResults[index] = firstPastThePost(v -> v.getBallotBox() == table);
            }
            LOGGER.debug("Unlocked table {}", index);
        }
        return finalTableResults[index];
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
        //      y van a entrar antes de que cierre la eleccion
        writeLock.lock();
        if(this.electionStatus==ElectionStatus.NOT_STARTED && electionStatus==ElectionStatus.STARTED ||
            this.electionStatus==ElectionStatus.STARTED && electionStatus==ElectionStatus.FINISHED){

            this.electionStatus = electionStatus;
            LOGGER.debug("Changed election status to {}. #Votes so far = {}", electionStatus, votes.size());

            if(electionStatus == ElectionStatus.FINISHED && votes.size() > 0) {
                // Inicializo el cache de los votos finales de cada mesa
                int n_tables = maxTable - minTable + 1;
                finalTableResults = new ElectionResults[n_tables];
                finalTableLocks = new Object[n_tables];
                for(int i = 0; i < n_tables; i++) {
                    finalTableLocks[i] = new Object(); // Lock
                }
            }
            writeLock.unlock();
        }
        else {
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
                writeLock.unlock();
                notifyVote(vote);
                break;
            case FINISHED:
                writeLock.unlock();
               throw new ElectionStateException("Can't register vote. Election has already finished");
        }

        // Esto sirve despues para el cache de las mesas
        if(vote.getBallotBox() < minTable || vote.getBallotBox() > maxTable) {
            synchronized (tableStatsLock) {
                minTable = Math.min(minTable, vote.getBallotBox());
                maxTable = Math.max(maxTable, vote.getBallotBox());
            }
        }
    }
}
