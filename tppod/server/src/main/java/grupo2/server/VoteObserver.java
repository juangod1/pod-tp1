package grupo2.server;

import grupo2.api.Vote;

import java.util.List;

public interface VoteObserver {
    void newVotes(List<Vote> vote);
    void newVote(Vote vote);
}
