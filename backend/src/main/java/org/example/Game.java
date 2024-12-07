package org.example;

import java.io.*;
import java.util.*;

public class Game {
private Deck adventureDeck;
private Deck eventDeck;
private static List<Player> players;
private int currentPlayerIndex;
private final List<Player> winners = new ArrayList<>();
private List<Player> stageWinners = new ArrayList<>();
private List<Player> stageParticipants = new ArrayList<>();
private int askPlayer = 3;
private List<Card> sponsorQuestCards = new ArrayList<>();
private List<Integer> stageValues = new ArrayList<>();
private List<Card> stageCards = new ArrayList<>();
private Set<String> weaponTypes = new HashSet<>();
private Card foeCard = null;
private Card newDrawnCard = new Card("Test", "Event", 0);
private Boolean doneStage = true;
private Boolean sponsorPromptStatus = false;
private int currentSponsor;
private int currentStageIndex = 1;
private List<Card> selectedCards = new ArrayList<>();
private Set<String> usedWeaponNames = new HashSet<>(); // Tracks the weapon names used in the current stage
private boolean endQuest = false;

public Game(List<String> playerNames) {
    System.out.println("Initializing the game...");

    this.adventureDeck = new Deck("Adventure");
    this.eventDeck = new Deck("Event");
    players = new ArrayList<>();

    for (String name : playerNames) {
        players.add(new Player(name));
    }

    this.currentPlayerIndex = 0;

    System.out.println("Players added: " + playerNames);
    System.out.println("Game setup complete.\n");
}

    //Main

    public String start(String input) {
        StringBuilder output = new StringBuilder();

        while (true) {
            output.append(playTurn());

            if (checkWinCondition()) {
                output.append(endGame());
                break;
            } else {
                output.append(players.get(currentPlayerIndex).getName())
                        .append(", press Enter to end your turn and move to the next player.\n");
                output.append(nextPlayer());
            }
        }

        return output.toString();
    }



    public String endGame() {
        StringBuilder output = new StringBuilder();

        if(checkWinCondition()){
            output.append("\nEnd Game!\n");
            if (!winners.isEmpty()) {
                output.append("We have ").append(winners.size()).append(" winner(s):\n");

                for (Player winner : winners) {
                    output.append("Winner: ").append(winner.getName())
                            .append(" with ").append(winner.getShields()).append(" shields!\n");
                }
            }
        }
         else {
            output.append("No winner was found.\n");
        }
        return output.toString();
    }


    public String setupGame() {
        StringBuilder output = new StringBuilder();

        output.append("\n--- Shuffling decks ---\n");
        adventureDeck.shuffle();
        eventDeck.shuffle();
        output.append("Decks shuffled.\n\n");

        output.append("Distributing 12 cards to each player...\n");
        for (Player player : players) {
            player.drawCards(adventureDeck, 12);
        }
        output.append("Initial card distribution complete.\n\n");

        return output.toString();
    }


    public String nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        return "Next player's turn.\n";
    }


    public String playTurn() {
        StringBuilder output = new StringBuilder();
        Player currentPlayer = players.get(currentPlayerIndex);
        endQuest = false;
        stageWinners.clear();
        output.append("\n\n--- Turn of ").append(currentPlayer.getName()).append(" ---\n\n");

        Card drawnCard = eventDeck.draw();
        output.append(currentPlayer.getName()).append("'s hand is ").append(currentPlayer.showHand()).append("\n\n");

        if (drawnCard != null) {
            output.append(currentPlayer.getName()).append(" draws an event card: ").append(drawnCard).append("\n");
            newDrawnCard = drawnCard;
            output.append(handleDrawnCard());
            if(doneStage){
                setDoneStage(false);
                nextPlayer();
            }
        } else {
            output.append("No event cards left in the deck.\n");
        }

        return output.toString();
    }

    //Main
    public String handleDrawnCard(){
        doneStage = false;
        StringBuilder output = new StringBuilder();
        Player currentPlayer = players.get(currentPlayerIndex);

        if (newDrawnCard.getName().startsWith("Q") && !newDrawnCard.getName().startsWith("Qu")) {

            sponsorQuestCards.clear();
            output.append("A quest card has been drawn!\n\n");
            output.append("Seeking a sponsor for the quest: ").append(newDrawnCard.getName()).append("\n\n");
            if(sponsorPromptStatus){
                sponsorPromptStatus = false;
                doneStage = true;
            }
        } else {
            output.append("An event card has been drawn!\n\n");
            output.append(handleEventCard(currentPlayer));
        }
        return output.toString();
    }

    public String handleEventCard(Player player) {
        StringBuilder output = new StringBuilder();

        switch (newDrawnCard.getName()) {
            case "Plague":
                player.loseShields(2);
                output.append("Event: Plague - ").append(player.getName()).append(" loses 2 shields.\n")
                        .append(player.getName()).append("'s remaining shields: ").append(player.getShields()).append("\n");
                doneStage = true;
                break;

            case "Queen's Favor":
                player.drawCards(adventureDeck, 2);
                output.append("Event: Queen's Favor - ").append(player.getName()).append(" draws 2 adventure cards.\n\n")
                        .append(player.getName()).append("'s updated hand: ").append(player.showHand()).append("\n\n");
                doneStage = true;
                break;

            case "Prosperity":
                output.append("Event: Prosperity - All players draw 2 adventure cards.\n\n");
                for (Player p : players) {
                    p.drawCards(adventureDeck, 2);
                    output.append(p.getName()).append("'s updated hand: ").append(p.showHand()).append("\n\n");
                }
                doneStage = true;
                break;

            default:
                output.append("Event: ").append(newDrawnCard.getName()).append(".\n");
        }

        return output.toString();
    }

    public String handleQuestCard(String input, int currentIndex) {
        StringBuilder output = new StringBuilder();

        Player currentPlayer = players.get(currentIndex);
        if (input.equalsIgnoreCase("y")) {
            currentSponsor = currentIndex;
            output.append(currentPlayer.getName())
                    .append(" will sponsor the quest ")
                    .append(newDrawnCard.getName())
                    .append("\n");
            setDoneStage(false);
            return output.toString();
        } else {
            output.append(currentPlayer.getName())
                    .append(" declines to sponsor the quest. Moving to the next player.").append("\n\n");
            int nextPlayerIndex = (currentIndex + 1) % players.size();
            if (nextPlayerIndex == currentIndex) {
                setDoneStage(true);
            }

            return output.toString();
        }
    }

    public String handleQuestSponsor(String input, int stageNumber){
        return sponsorSetupQuest(sponsorQuestCards, stageValues, input, stageNumber);}

    public String sponsorSetupQuest(List<Card> sponsorQuestCards, List<Integer> stageValues, String input, int stageNumber) {
        StringBuilder output = new StringBuilder();
        output.append("\nSetting up stage ").append(stageNumber).append(" of ").append(newDrawnCard.getValue()).append("\n");

        String stageSetupResult = sponsorSetupQuestStage(getPlayerByIndex(currentSponsor), stageValues, sponsorQuestCards, input);
        output.append(stageSetupResult);

        if (stageSetupResult.contains("Stage setup complete")) {
            if (stageNumber == newDrawnCard.getValue()) {
                output.append("\nQuest setup complete with ").append(stageValues.size()).append(" stages.\n");
                output.append("\nStage values: ").append(stageValues).append("\n");
            } else {
                output.append("\nStage ").append(stageNumber).append(" setup complete. Proceed to the next stage.\n\n");
            }
        } else if (stageSetupResult.contains("\nInvalid stage.\n")) {
            output.append("Warning: Stage setup is invalid. Continue adding cards or type 'quit' to finalize.\n");
        }

        return output.toString();
    }


    public String sponsorSetupQuestStage(Player sponsor, List<Integer> stageValues, List<Card> sponsorQuestCards, String input) {
        StringBuilder output = new StringBuilder();
        if (input.equalsIgnoreCase("quit")) {
            if (validateStage(foeCard)) {
                int stageValue = calculateStageValue(foeCard, stageCards);

                if (stageValues.isEmpty() || stageValue > stageValues.get(stageValues.size() - 1)) {
                    stageValues.add(stageValue);
                    sponsorQuestCards.addAll(stageCards);

                    for (Card card : stageCards) {
                        sponsor.getHand().remove(card);
                    }

                    sponsor.getHand().remove(stageCards);
                    output.append("Stage setup complete. Stage value: ").append(stageValue).append("\n");
                    output.append("Cards used for this stage: ").append(stageCards).append("\n");
                    stageCards.clear();
                    weaponTypes.clear();
                    foeCard = null;
                } else {
                    output.append("\nStage value must be greater than the previous stage.\n");
                    System.out.println(stageValues);

                }
            } else {
                output.append("\nWarning: Invalid stage. A stage cannot be empty.\n");
            }
        } else {
            output.append(processSelectedCard(input, sponsor, stageCards, weaponTypes, output));
        }

        return output.toString();
    }

    private String processSelectedCard(String input, Player sponsor, List<Card> stageCards, Set<String> weaponTypes, StringBuilder output) {
        try {
            int position = Integer.parseInt(input) - 1;
            if (position >= 0 && position < sponsor.getHandSize()) {
                Card selectedCard = sponsor.getHand().get(position);

                if (selectedCard.getType().equals("Foe")) {
                    if (stageCards.stream().anyMatch(card -> card.getType().equals("Foe"))) {
                        return "\nWarning: A stage can only have one Foe card. Please choose another card.\n";
                    } else {
                        foeCard = selectedCard;
                        stageCards.add(selectedCard);
//                        sponsor.getHand().remove(foeCard);
                        return selectedCard.getName() + " (Foe) added to the stage.\n";
                    }
                } else if (selectedCard.getType().equals("Weapon")) {
                    if (weaponTypes.contains(selectedCard.getName())) {
                        return "\nWarning: A stage cannot have repeated Weapon cards. Please choose another card.\n";
                    } else {
                        weaponTypes.add(selectedCard.getName());
                        stageCards.add(selectedCard);
//                        sponsor.getHand().remove(selectedCard);
                        return selectedCard.getName() + " (Weapon) added to the stage.\n";
                    }
                } else {
                    return "\nYou can only add Foe or Weapon cards to a stage.\n";
                }
            } else {
                return "\nInvalid position. Please try again.\n";
            }
        } catch (NumberFormatException e) {
            return "\nInvalid input. Please enter a number.\n";
        }
    }

    private boolean validateStage(Card foeCard) {
        return foeCard != null;
    }

    private int calculateStageValue(Card foeCard, List<Card> stageCards) {
        int totalValue = foeCard.getValue();
        for (Card card : stageCards) {
            if (card.getType().equals("Weapon")) {
                totalValue += card.getValue();
            }
        }
        return totalValue;
    }

    //PARTICIPANT
    public String handlePlayerParticipation(int currentPlayerIndex, String input) {
        StringBuilder output = new StringBuilder();

        if (askPlayer > 0) {
            output.append(promptPlayersForParticipation(currentPlayerIndex, input));
            askPlayer--;

            if (askPlayer == 0) {
                if (stageParticipants.isEmpty()) {
                    output.append("\n\nNo participants decided to join the quest. The quest has failed.\n\n");
                    output.append(handleQuestEnd());
                    return output.toString();
                } else {
                    if (currentStageIndex >= 1 && currentStageIndex < stageValues.size()) {
                        int stageValue = stageValues.get(currentStageIndex-1);
                        output.append("\n\nResolving stage ").append(currentStageIndex).append(" with value ").append(stageValue).append("\n\n");

                        output.append(drawCardsForParticipants());
                    }
                }
                return output.toString();
            }
        }
        return output.toString();
    }

    public String handlePlayerAttack(int currentPlayer, String input){
        StringBuilder output = new StringBuilder();
        output.append(resolveStage(input, currentPlayer));
        return output.toString();
    }

    public String handleStageResolve(int currentPlayerIndex, String input) {
        StringBuilder output = new StringBuilder();

        if (stageWinners.isEmpty()) {
            output.append("No participants can continue. The quest has failed.\n");
            output.append(handleQuestEnd());
            return output.toString();
        }

        output.append(promptStageContinuation(currentPlayerIndex, input));
        askPlayer--;


        if (askPlayer <= 0) {
            // Only resolve the stage if all participants have been prompted
            if (!isFinalStage(currentStageIndex)) {
                currentStageIndex++;
                askPlayer = stageWinners.size();
            }
        }

        return output.toString();
    }



    public String handleQuestEnd() {
        StringBuilder output = new StringBuilder();
        endQuest = true;
        Player sponsor = getPlayers().get(currentSponsor);
        output.append("\n\nThe quest has ended. ").append(sponsor.getName()).append(" discards all cards used to build the quest.\n\n");

        int cardsUsed = sponsorQuestCards.size();

        int sponsorDrawCards = cardsUsed + getNewDrawnCard();
        System.out.println(sponsorQuestCards);
        output.append(sponsor.getName()).append(" draws ").append(sponsorDrawCards).append(" cards for sponsoring the quest.\n");

        if (adventureDeck.isEmpty()) {
            output.append("Adventure deck is empty, reshuffling the discard pile...\n");
            adventureDeck.reshuffleDiscardPile();
        }

        sponsor.drawCards(adventureDeck, sponsorDrawCards);
        output.append(sponsor.getName()).append("'s updated hand: ").append(sponsor.showHand()).append("\n");

        askPlayer = 3;
        currentStageIndex = 1;
        stageParticipants.clear();
        foeCard = null;
        usedWeaponNames.clear();
        selectedCards.clear();
        stageValues.clear();
        return output.toString();
    }


    private boolean isFinalStage(int stageIndex) {
        return stageIndex == getNewDrawnCard();
    }

    private String awardShieldsToWinners(Player winner, int shieldsEarned) {
        StringBuilder output = new StringBuilder();

        output.append(winner.getName()).append(" has won the quest and earned ").append(shieldsEarned).append(" shields!\n\n");
        winner.addShields(shieldsEarned);

        return output.toString();
    }

    private Player getCurrentWinner(int currentPlayerIndex) {
        return stageWinners.get(currentPlayerIndex);
    }

    private String promptStageContinuation(int currentPlayerIndex, String input) {
        StringBuilder output = new StringBuilder();
        Player currentWinner = getPlayers().get(currentPlayerIndex);


        if (input.equalsIgnoreCase("y")) {
            output.append(currentWinner.getName()).append(" has chosen to participate in the next stage.\n\n");
        } else {
            stageParticipants.remove(currentWinner);
            if(stageWinners.contains(currentWinner)) stageWinners.remove(currentWinner);
            output.append(currentWinner.getName()).append(" has decided to withdraw.\n\n");
        }

        return output.toString();
    }

    public String resolveStage(String input, int currentPlayerIndex) {
        StringBuilder output = new StringBuilder();

        Player participant = getPlayerByIndex(currentPlayerIndex);

        Map<String, Object> attackResult = prepareAttack(participant, input);
        List<Card> usedCards = (List<Card>) attackResult.get("selectedCards");
        String attackMessage = (String) attackResult.get("message");

        output.append(attackMessage);

        int attackValue = calculateAttackValue(usedCards);


        if (input.equalsIgnoreCase("quit")){
            output.append(participant.getName()).append(" prepared an attack with value: ").append(attackValue).append("\n");
            if (attackValue >= stageValues.get(currentStageIndex - 1)) {
//                output.append(participant.getName()).append("'s updated hand: ").append(participant.showHand());
                output.append(participant.getName()).append(" succeeds with an attack value of ").append(attackValue).append("!\n\n");
                for (Card card : selectedCards) {
                    participant.getHand().remove(card);
                }
                if(!stageWinners.contains(participant)) stageWinners.add(participant);
                if (isFinalStage(currentStageIndex)){
                    output.append(awardShieldsToWinners(participant, stageValues.size()));
                }
                if (participant.equals(stageWinners.get(stageWinners.size() - 1)) && isFinalStage(currentStageIndex)) output.append(handleQuestEnd());
            } else {
                stageParticipants.remove(participant);
//                output.append(participant.getName()).append("'s updated hand: ").append(participant.showHand());
                output.append(participant.getName()).append(" fails with an attack value of ").append(attackValue).append(".\n\n");
                if(stageWinners.contains(participant)) stageWinners.remove(participant);
            }

            for (Card card : selectedCards) {
                participant.getHand().remove(card);
            }
            selectedCards.clear();
            usedWeaponNames.clear();
        }

        return output.toString();
    }



    public int calculateAttackValue(List<Card> cards) {
        return cards.stream().mapToInt(Card::getValue).sum();
    }


    public Map<String, Object> prepareAttack(Player participant, String input) {
        StringBuilder output = new StringBuilder();

        if (input.equalsIgnoreCase("quit")) {
            output.append(participant.getName()).append(" has chosen to end their attack turn.\n");
            Map<String, Object> result = new HashMap<>();
            result.put("message", output.toString());
            result.put("selectedCards", selectedCards);
            return result;
        }

        try {
            int position = Integer.parseInt(input) - 1;

            if (position >= 0 && position < participant.getHandSize()) {
                Card selectedCard = participant.getHand().get(position);

                if (selectedCard.getType().equals("Weapon")) {
                    if (usedWeaponNames.contains(selectedCard.getName())) {
                        output.append("\nInvalid selection: A stage cannot have repeated Weapon cards. Please choose another card.\n\n");
                    } else {
                        selectedCards.add(selectedCard);
                        usedWeaponNames.add(selectedCard.getName());
                        output.append(selectedCard.getName()).append(" (Weapon) added to your attack.\n\n");
                    }
                } else {
                    output.append("\nInvalid selection: You can only use Weapon cards for an attack.\n\n");
                }
            } else {
                output.append("\nInvalid position: Please choose a valid card index.\n\n");
            }
        } catch (NumberFormatException e) {
            output.append("\nInvalid input: Please enter a card position as a number.\n\n");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("message", output.toString());
        result.put("selectedCards", selectedCards);

        return result;
    }


    public String drawCardsForParticipants() {
        StringBuilder output = new StringBuilder();
        System.out.println(stageParticipants);

        for(Player participant : stageParticipants){
            participant.drawCards(adventureDeck, 1);
            output.append(participant.getName()).append(" draws 1 adventure card.\n");
        }
        return output.toString();
    }


    private String promptPlayersForParticipation(int currentPlayerIndex, String input) {
        StringBuilder output = new StringBuilder();

        Player currentPlayer = getPlayers().get(currentPlayerIndex);

        if (!currentPlayer.equals(getPlayerByIndex(currentSponsor))) {
            if (input.equalsIgnoreCase("y")) {
                stageParticipants.add(currentPlayer);
                output.append(currentPlayer.getName()).append(" has chosen to participate.");

            } else {
                output.append(currentPlayer.getName()).append(" has declined to participate.");
            }
        }

        return output.toString();
    }

    private boolean checkWinCondition() {
        for (Player player : players) {
            if (player.getShields() >= 7) {
                winners.add(player);
            }
        }
        return !winners.isEmpty();
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public List<Player> getStageWinners(){
        return stageWinners;
    }

    public int getCurrentPlayerIndex(){
        return currentPlayerIndex;
    }
    public List<Player> getPlayers() {
        return players;
    }

    public Deck getAdventureDeck() {
        return this.adventureDeck;
    }

    public Deck getEventDeck(){
        return this.eventDeck;
    }

    public List<Player> getParticipants() {
        return stageParticipants;
    }

    public List<Integer> getStageValues(){
        return stageValues;
    }

    public boolean getDoneStage(){
        return doneStage;
    }

    public void setDoneStage(boolean value){
        doneStage = value;
    }

    public Player getPlayerByIndex(int i){
        return getPlayers().get(i);
    }

    public int getCurrentSponsor(){return currentSponsor;}

    public int getNewDrawnCard(){return newDrawnCard.getValue();}

    public void rigScenarios(int scenario){
        rigPlayerHands(scenario);
        setUpAdventureDeck(scenario);
        setUpQuestCards(scenario);
    }


    public void rigPlayerHands(int scenario) {
        switch (scenario) {
            case 1:
                getPlayers().get(0).setHand(List.of(
                        new Card("F5", "Foe", 5), new Card("F5", "Foe", 5), new Card("F15", "Foe", 15),
                        new Card("F15", "Foe", 15), new Card("D5", "Weapon", 5), new Card("S10", "Weapon", 10),
                        new Card("S10", "Weapon", 10), new Card("H10", "Weapon", 10), new Card("H10", "Weapon", 10),
                        new Card("B15", "Weapon", 15), new Card("B15", "Weapon", 15), new Card("L20", "Weapon", 20)
                ));
                getPlayers().get(1).setHand(List.of(
                        new Card("F5", "Foe", 5), new Card("F5", "Foe", 5), new Card("F20", "Foe", 20),
                        new Card("F30", "Foe", 30), new Card("D5", "Weapon", 5), new Card("S10", "Weapon", 10),
                        new Card("S10", "Weapon", 10), new Card("S10", "Weapon", 10), new Card("H10", "Weapon", 10),
                        new Card("H10", "Weapon", 10), new Card("L20", "Weapon", 20), new Card("L20", "Weapon", 20)
                ));
                getPlayers().get(2).setHand(List.of(
                        new Card("F5", "Foe", 5), new Card("F5", "Foe", 5), new Card("F5", "Foe", 5),
                        new Card("F15", "Foe", 15), new Card("D5", "Weapon", 5), new Card("S10", "Weapon", 10),
                        new Card("S10", "Weapon", 10), new Card("S10", "Weapon", 10), new Card("H10", "Weapon", 10),
                        new Card("H10", "Weapon", 10), new Card("B15", "Weapon", 15), new Card("L20", "Weapon", 20)
                ));
                getPlayers().get(3).setHand(List.of(
                        new Card("F5", "Foe", 5), new Card("F15", "Foe", 15), new Card("F15", "Foe", 15),
                        new Card("F40", "Foe", 40), new Card("D5", "Weapon", 5), new Card("D5", "Weapon", 5),
                        new Card("S10", "Weapon", 10), new Card("H10", "Weapon", 10), new Card("H10", "Weapon", 10),
                        new Card("B15", "Weapon", 15), new Card("L20", "Weapon", 20), new Card("E30", "Weapon", 30)
                ));
                break;

            case 2:
                getPlayers().get(0).setHand(List.of(
                        new Card("F5", "Foe", 5), new Card("F5", "Foe", 5), new Card("F10", "Foe", 10),
                        new Card("F10", "Foe", 10), new Card("F15", "Foe", 15), new Card("F15", "Foe", 15),
                        new Card("D5", "Weapon", 5), new Card("H10", "Weapon", 10), new Card("H10", "Weapon", 10),
                        new Card("B15", "Weapon", 15), new Card("B15", "Weapon", 15), new Card("L20", "Weapon", 20)
                ));
                getPlayers().get(1).setHand(List.of(
                        new Card("F40", "Foe", 40), new Card("F50", "Foe", 50), new Card("H10", "Weapon", 10),
                        new Card("H10", "Weapon", 10), new Card("S10", "Weapon", 10), new Card("S10", "Weapon", 10),
                        new Card("S10", "Weapon", 10), new Card("B15", "Weapon", 15), new Card("B15", "Weapon", 15),
                        new Card("L20", "Weapon", 20), new Card("L20", "Weapon", 20), new Card("E30", "Weapon", 30)
                ));
                getPlayers().get(2).setHand(List.of(
                        new Card("F5", "Foe", 5), new Card("F5", "Foe", 5), new Card("F5", "Foe", 5),
                        new Card("F5", "Foe", 5), new Card("D5", "Weapon", 5), new Card("D5", "Weapon", 5),
                        new Card("D5", "Weapon", 5), new Card("H10", "Weapon", 10), new Card("H10", "Weapon", 10),
                        new Card("H10", "Weapon", 10), new Card("H10", "Weapon", 10), new Card("H10", "Weapon", 10)
                ));
                getPlayers().get(3).setHand(List.of(
                        new Card("F50", "Foe", 50), new Card("F70", "Foe", 70), new Card("H10", "Weapon", 10),
                        new Card("H10", "Weapon", 10), new Card("S10", "Weapon", 10), new Card("S10", "Weapon", 10),
                        new Card("S10", "Weapon", 10), new Card("B15", "Weapon", 15), new Card("B15", "Weapon", 15),
                        new Card("L20", "Weapon", 20), new Card("L20", "Weapon", 20), new Card("E30", "Weapon", 30)
                ));
                break;

            case 3:
                getPlayers().get(0).setHand(List.of(
                        new Card("F5", "Foe", 5), new Card("F5", "Foe", 5), new Card("F10", "Foe", 10),
                        new Card("F10", "Foe", 10), new Card("F15", "Foe", 15), new Card("F15", "Foe", 15),
                        new Card("F20", "Foe", 20), new Card("F20", "Foe", 20), new Card("D5", "Weapon", 5),
                        new Card("D5", "Weapon", 5), new Card("D5", "Weapon", 5), new Card("D5", "Weapon", 5)
                ));
                getPlayers().get(1).setHand(List.of(
                        new Card("F25", "Foe", 25), new Card("F30", "Foe", 30), new Card("H10", "Weapon", 10),
                        new Card("H10", "Weapon", 10), new Card("S10", "Weapon", 10), new Card("S10", "Weapon", 10),
                        new Card("S10", "Weapon", 10), new Card("B15", "Weapon", 15), new Card("B15", "Weapon", 15),
                        new Card("L20", "Weapon", 20), new Card("L20", "Weapon", 20), new Card("E30", "Weapon", 30)
                ));
                getPlayers().get(2).setHand(List.of(
                        new Card("F25", "Foe", 25), new Card("F30", "Foe", 30), new Card("H10", "Weapon", 10),
                        new Card("H10", "Weapon", 10), new Card("S10", "Weapon", 10), new Card("S10", "Weapon", 10),
                        new Card("S10", "Weapon", 10), new Card("B15", "Weapon", 15), new Card("B15", "Weapon", 15),
                        new Card("L20", "Weapon", 20), new Card("L20", "Weapon", 20), new Card("E30", "Weapon", 30)
                ));
                getPlayers().get(3).setHand(List.of(
                        new Card("F25", "Foe", 25), new Card("F30", "Foe", 30), new Card("F70", "Foe", 70),
                        new Card("H10", "Weapon", 10), new Card("H10", "Weapon", 10), new Card("S10", "Weapon", 10),
                        new Card("S10", "Weapon", 10), new Card("S10", "Weapon", 10), new Card("B15", "Weapon", 15),
                        new Card("B15", "Weapon", 15), new Card("L20", "Weapon", 20), new Card("L20", "Weapon", 20)
                ));
                break;

            case 4:
                getPlayers().get(0).setHand(List.of(
                        new Card("F50", "Foe", 50), new Card("F70", "Foe", 70), new Card("D5", "Weapon", 5),
                        new Card("D5", "Weapon", 5), new Card("S10", "Weapon", 10), new Card("S10", "Weapon", 10),
                        new Card("H10", "Weapon", 10), new Card("H10", "Weapon", 10), new Card("B15", "Weapon", 15),
                        new Card("B15", "Weapon", 15),  new Card("L20", "Weapon", 20), new Card("L20", "Weapon", 20)
                ));
                getPlayers().get(1).setHand(List.of(
                        new Card("F5", "Foe", 5), new Card("F5", "Foe", 5), new Card("F10", "Foe", 10),
                        new Card("F15", "Foe", 15), new Card("F15", "Foe", 15), new Card("F20", "Foe", 20),
                        new Card("F20", "Foe", 20), new Card("F25", "Foe", 25), new Card("F30", "Foe", 30),
                        new Card("F30", "Foe", 30), new Card("F40", "Foe", 40), new Card("E30", "Weapon", 30)
                ));
                getPlayers().get(2).setHand(List.of(
                        new Card("F5", "Foe", 5), new Card("F5", "Foe", 5), new Card("F10", "Foe", 10),
                        new Card("F15", "Foe", 15), new Card("F15", "Foe", 15), new Card("F20", "Foe", 20),
                        new Card("F20", "Foe", 20), new Card("F25", "Foe", 25), new Card("F25", "Foe", 25),
                        new Card("F30", "Foe", 30), new Card("F40", "Foe", 40), new Card("L20", "Weapon", 20)
                ));
                getPlayers().get(3).setHand(List.of(
                        new Card("F5", "Foe", 5), new Card("F5", "Foe", 5), new Card("F10", "Foe", 10),
                        new Card("F15", "Foe", 15), new Card("F15", "Foe", 15), new Card("F20", "Foe", 20),
                        new Card("F20", "Foe", 20), new Card("F25", "Foe", 25), new Card("F25", "Foe", 25),
                        new Card("F30", "Foe", 30), new Card("F40", "Foe", 40), new Card("E30", "Weapon", 30)
                ));
                break;




            default:
                throw new IllegalArgumentException("Invalid scenario: " + scenario);
        }
    }


    public void setUpAdventureDeck(int scenario){
            List<Card> riggedDraws = switch (scenario) {
                case 1 -> List.of(
                        new Card("F30", "Foe", 30), new Card("S10", "Weapon", 10), new Card("B15", "Weapon", 15),
                        new Card("F10", "Foe", 10), new Card("L20", "Weapon", 20), new Card("L20", "Weapon", 20),
                        new Card("B15", "Weapon", 15), new Card("S10", "Weapon", 10), new Card("F30", "Foe", 30),
                        new Card("L20", "Weapon", 20), new Card("S10", "Weapon", 10), new Card("F30", "Foe", 30),
                        new Card("L20", "Weapon", 20), new Card("S10", "Weapon", 10), new Card("F30", "Foe", 30),
                        new Card("L20", "Weapon", 20), new Card("S10", "Weapon", 10), new Card("F30", "Foe", 30),
                        new Card("L20", "Weapon", 20), new Card("S10", "Weapon", 10), new Card("F30", "Foe", 30),
                        new Card("L20", "Weapon", 20), new Card("S10", "Weapon", 10), new Card("F30", "Foe", 30),
                        new Card("L20", "Weapon", 20)
                );
                case 2 -> List.of(
                        new Card("F5", "Foe", 5), new Card("F40", "Foe", 40), new Card("F10", "Foe", 10),
                        new Card("F10", "Foe", 10), new Card("F30", "Foe", 30), new Card("F30", "Foe", 30),
                        new Card("F15", "Foe", 15), new Card("F15", "Foe", 15), new Card("F20", "Foe", 20),
                        new Card("F5", "Foe", 5), new Card("F10", "Foe", 10), new Card("F15", "Foe", 15),
                        new Card("F15", "Foe", 15), new Card("F20", "Foe", 20), new Card("F20", "Foe", 20),
                        new Card("F20", "Foe", 20), new Card("F20", "Foe", 20), new Card("F25", "Foe", 25),
                        new Card("F25", "Foe", 25), new Card("F30", "Foe", 30), new Card("D5", "Weapon", 5),
                        new Card("D5", "Weapon", 5), new Card("F15", "Foe", 15), new Card("F15", "Foe", 15),
                        new Card("F25", "Foe", 25), new Card("F25", "Foe", 25), new Card("F20", "Foe", 20),
                        new Card("F20", "Foe", 20), new Card("F25", "Foe", 25), new Card("F30", "Foe", 30),
                        new Card("S10", "Weapon", 10), new Card("B15", "Weapon", 15), new Card("B15", "Weapon", 15),
                        new Card("L20", "Weapon", 20)
                );

                case 3 -> List.of(
                        new Card("F5", "Foe", 5), new Card("F10", "Foe", 10), new Card("F20", "Foe", 20),
                        new Card("F15", "Foe", 15), new Card("F5", "Foe", 5), new Card("F25", "Foe", 25),
                        new Card("F5", "Foe", 5), new Card("F10", "Foe", 15), new Card("F20", "Foe", 20),
                        new Card("F5", "Foe", 5), new Card("F10", "Foe", 10), new Card("F20", "Foe", 20),
                        new Card("F5", "Foe", 5), new Card("F5", "Foe", 5), new Card("F10", "Foe", 10),
                        new Card("F10", "Foe", 10), new Card("F15", "Foe", 15), new Card("F15", "Foe", 15),
                        new Card("F15", "Foe", 15), new Card("F15", "Foe", 15), new Card("F25", "Foe", 25),
                        new Card("F25", "Foe", 25), new Card("H10", "Weapon", 10), new Card("S10", "Weapon", 10),
                        new Card("B15", "Weapon", 15), new Card("F40", "Foe", 40), new Card("D5", "Weapon", 5),
                        new Card("D5", "Weapon", 5), new Card("F30", "Foe", 30), new Card("F25", "Foe", 25),
                        new Card("B15", "Weapon", 15), new Card("H10", "Weapon", 10), new Card("F50", "Foe", 50),
                        new Card("S10", "Weapon", 10), new Card("S10", "Weapon", 10), new Card("F40", "Foe", 40),
                        new Card("F50", "Foe", 50), new Card("H10", "Weapon", 10), new Card("H10", "Weapon", 10),
                        new Card("H10", "Weapon", 10), new Card("S10", "Weapon", 10), new Card("S10", "Weapon", 10),
                        new Card("S10", "Weapon", 10), new Card("S10", "Weapon", 10), new Card("F35", "Foe", 35)
                );
                case 4 -> List.of(
                        new Card("F5", "Foe", 5), new Card("F15", "Foe", 15), new Card("F10", "Foe", 10),
                        new Card("F5", "Foe", 5), new Card("F10", "Foe", 10), new Card("F15", "Foe", 15),
                        new Card("D5", "Weapon", 5), new Card("D5", "Weapon", 5), new Card("D5", "Weapon", 5),
                        new Card("D5", "Weapon", 5), new Card("H10", "Weapon", 10),new Card("H10", "Weapon", 10),
                        new Card("H10", "Weapon", 10), new Card("H10", "Weapon", 10), new Card("S10", "Weapon", 10),
                        new Card("S10", "Weapon", 10), new Card("S10", "Weapon", 10)
                );
                default -> throw new IllegalArgumentException("Invalid scenario: " + scenario);
            };
            getAdventureDeck().stackCardsOnTop(riggedDraws);
    }

    public void setUpQuestCards(int scenario){
        List<Card> Quest = switch (scenario) {
            case 1 -> List.of(
                    new Card("Q4", "Quest", 4),
                    new Card("Q4", "Quest", 4)

                    );

            case 2 -> List.of(
                    new Card("Q4", "Quest", 4),
                    new Card("Q3", "Quest", 3)
                    );
            case 3 -> List.of(
                    new Card("Q4", "Quest", 4),
                    new Card("Plague", "Event", -2),
                    new Card("Prosperity", "Event",2),
                    new Card("Queen's Favor", "Event",2 ),
                    new Card("Q3", "Quest", 3)
            );

            case 4 -> List.of(
                    new Card("Q2", "Quest", 2)
                );
            default  -> throw new IllegalArgumentException("Invalid scenario: " + scenario);
        };
        getEventDeck().stackCardsOnTop(Quest);
    }

    public boolean getEndQuest(){
        return endQuest;
    }

}
