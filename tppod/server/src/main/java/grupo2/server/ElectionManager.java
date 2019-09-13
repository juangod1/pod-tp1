package grupo2.server;

import grupo2.api.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// Aca va toda la sincronización
// Cada servicio se registra como listener aca para ciertos eventos y tambien notifica de
// los eventos de los cuales es responsable.
public class ElectionManager {

    private static final int NUMBER_OF_PROVINCIAL_WINNERS = 5;
    private final List<VoteObserver> observers = new ArrayList<>();
    private final List<Vote> votes = new ArrayList<>();
    private final Lock readLock, writeLock;
    private ElectionStatus electionStatus;


    public ElectionManager() {
        ReadWriteLock rwLock = new ReentrantReadWriteLock();
        readLock = rwLock.readLock();
        writeLock = rwLock.writeLock();
    }

    public void register(VoteObserver observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public boolean notifyVote(Vote vote) {
        writeLock.lock();
        // TODO: Si la eleccion ya cerro devolver un error de "muy tarde amigo" (retornar false)
        // Es importante tomar el lock antes de leer el estado de la elección
        // aunque sea una operación de lectura, porque quizas espero el lock de write
        // atras de alguien que cerró la elección
        votes.add(vote);
        writeLock.unlock();
        observers.forEach(vo -> vo.newVote(vote)); //TODO: Tirar threads quizas overkill?
        return true;
    }

    public ElectionResults getNationalResults() {
        readLock.lock();
        ElectionResults results = null;
        switch (electionStatus) {
            case NOT_STARTED: throw new IllegalStateException("TODO");
            case STARTED: results = firstPastThePost(v -> true);
                break;
            case FINISHED: results = nationalFinalVote();
                break;
        }
        readLock.unlock();
        return results;
    }

    public ElectionResults getProvincialResults(Province p) {
        readLock.lock();
        ElectionResults results = null;
        switch (electionStatus) {
            case NOT_STARTED: throw new IllegalStateException("TODO");
            case STARTED: results = firstPastThePost(v -> v.getProvince() == p);
                break;
            case FINISHED: results = provincialFinalVote(p);
                break;
        }
        readLock.unlock();
        return results;
    }


    public ElectionResults getTableResults(int table) {
        return firstPastThePost(v -> v.getBallotBox() == table);
    }

    private ElectionResults firstPastThePost(Predicate<Vote> filterPredicate) {
        double size = votes.stream()
                .filter(filterPredicate)
                .count();

        Map<Party, Double> nationalResults = votes.stream()
                .filter(filterPredicate)
                .map(Vote::getTopVote)
                .collect(Collectors.groupingBy(Function.identity(),
                        Collectors.collectingAndThen(Collectors.counting(), x -> x/size)));
        return new ElectionResults(nationalResults, electionStatus);
    }


    private ElectionResults nationalFinalVote() {
        return alternativeVoting(votes, x -> true);  // Empiezo con todos los votos y sin filtrar
    }

    private ElectionResults provincialFinalVote(Province province) {
        List<Vote> provinceVotes = votes.stream().filter(v -> v.getProvince() == province).collect(Collectors.toList());
        SingleTransferableVoteCalculator calculator = new SingleTransferableVoteCalculator(provinceVotes, NUMBER_OF_PROVINCIAL_WINNERS);
        return new ElectionResults(calculator.calculate(), electionStatus);
    }

    private Optional<Party> getChoice(Vote v, Predicate<Party> filter) {
        return v.getRanking().stream().filter(filter).findFirst();
    }


    private ElectionResults alternativeVoting(List<Vote> validVotes, Predicate<Party> filterPredicate) {
        // Alternative voting system
        double voteCount = this.votes.size();   // Los porcentajes son siempre sobre el total (aun filtrando blancos)

        List<Vote> filteredVotes = // Sacamos los votos "en blanco" que pueden aparecer
                validVotes.stream()
                    .filter(v -> getChoice(v, filterPredicate).isPresent())
                    .collect(Collectors.toList());

        List<Map.Entry<Party, Long>> partySortedCount =
                filteredVotes.stream()
                    .map(v -> getChoice(v, filterPredicate).get()) // Ya estan filtrados, el get no falla
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet()
                    .stream()
                    .sorted(Comparator.comparingLong(Map.Entry::getValue))
                    .collect(Collectors.toList());

        // partySortedCount has the most voted last
        double maxPercentage = partySortedCount.get(partySortedCount.size() - 1).getValue()/voteCount;

        if (maxPercentage > 0.5 || partySortedCount.size() == 1) {
            Map<Party, Double> results = new HashMap<>();
            results.put(partySortedCount.get(partySortedCount.size() - 1).getKey(), maxPercentage);
            return new ElectionResults(results, electionStatus);
        } else {
            Party worst = partySortedCount.get(0).getKey(); // El primero es el peor
            Predicate<Party> newFilter = filterPredicate.and(p -> p != worst);  // La proxima iteracion no considera este partido
            return alternativeVoting(filteredVotes, newFilter);
        }
    }



    public ElectionStatus getElectionStatus() {
        return electionStatus;
    }

    public void setElectionStatus(ElectionStatus electionStatus) {
        // Usar el mismo writeLock que para la lista de votos asegura un par de cosas
        //   1. Si alguien esta consultando la lista de votos para computar resultados parciales
        //      entonces no se le "cierra" la eleccion en el medio
        //   2. Si hay votos todavia pendientes de emision, van a estar en la cola de este lock
        //      y van a entrar antes de que cierre la eleccioon
        writeLock.lock();
        this.electionStatus = electionStatus;
        writeLock.unlock();
    }
}
