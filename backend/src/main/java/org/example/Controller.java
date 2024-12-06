package org.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://127.0.0.1:8081")
public class Controller {

    private Game game;
    @GetMapping("/setupGame")
    public String setupGame() {
        List<String> playerNames = Arrays.asList("Player 1", "Player 2", "Player 3", "Player 4");
        game = new Game(playerNames);
        return game.setupGame();
    }

    @GetMapping("/playTurn")
    public String playTurn() {
        return game.playTurn();
    }

    @GetMapping("/showAllHands")
    public String showAllHands(){
        StringBuilder result = new StringBuilder();
        for(Player p : game.getPlayers()){
            result.append(p.getName()).append(p.getHandSize());
        }
        return result.toString();
    }


    @GetMapping("/promptSponsor")
    public String promptSponsor(@RequestParam String input, @RequestParam int currentIndex) {
        return game.handleQuestCard(input, currentIndex);
    }

    @GetMapping("/trimHandPlayer")
    public String trimHandPlayer(@RequestParam String input, @RequestParam int player) {
        StringBuilder result = new StringBuilder();
        result.append(game.getPlayers().get(player).trimHand(input)).append("\n");
        return result.toString();
    }

    @GetMapping("/checkPlayerHands")
    public List<Integer> checkPlayerHands() {
        List<Integer> playersToTrim = new ArrayList<>();

        // Iterate through each player and check the number of cards in their hand
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player player = game.getPlayers().get(i);

            // Check if the player has more than 12 cards
            if (player.getHand().size() > 12) {
                playersToTrim.add(i);  // Add the player's index (or name) to the list
            }
        }

        return playersToTrim;
    }

    @GetMapping("/getHand")
    public String getHand(@RequestParam int player) {
        Player p = game.getPlayers().get(player);
        return p.showHand();
    }

    @GetMapping("/getCurrentPlayerIndex")
    public int getCurrentPlayerIndex(){
        return game.getCurrentPlayerIndex();
    }


    @PostMapping("/setDoneStage")
    public String setDoneStage(@RequestParam boolean doneStage) {
        game.setDoneStage(doneStage);
        return "doneStage updated to " + doneStage;
    }

    @GetMapping("/getShield")
    public int getShield(){
        return game.getCurrentPlayer().getShields();
    }

    @GetMapping("/getPlayerShield")
    public int getPlayerShield(@RequestParam int player){
        return game.getPlayers().get(player).getShields();
    }

    @GetMapping("/nextPlayer")
    public String nextPlayer() {
        return game.nextPlayer();
    }

    @GetMapping("/getNewDrawnCard")
    public int getNewDrawnCard(){
        return game.getNewDrawnCard();
    }

    @GetMapping("/handleQuestSponsor")
    public String handleQuestSponsor(@RequestParam String input, @RequestParam int stageNumber){
        return game.handleQuestSponsor(input, stageNumber);
    }

    @GetMapping("/getCurrentSponsor")
    public int getCurrentSponsor(){
        return game.getCurrentSponsor();
    }

    @GetMapping("/handlePlayerParticipation")
    public String handlePlayerParticipation(@RequestParam int currentPlayerIndex, @RequestParam String input){
        return game.handlePlayerParticipation(currentPlayerIndex, input);
    }

    @GetMapping("/drawCardsForParticipants")
    public String drawCardsForParticipants(){
        return game.drawCardsForParticipants();
    }

    @GetMapping("/getStageValues")
    public List<Integer> getStageValues() {
        return game.getStageValues();
    }

    @GetMapping("/getParticipants")
    public List<Player> getParticipants() {
        return game.getParticipants();
    }

    @GetMapping("/handlePlayerAttack")
    public String handlePlayerAttack(@RequestParam int currentPlayer, @RequestParam String input) {
        return game.handlePlayerAttack(currentPlayer, input);
    }

    @GetMapping("/handleStageResolve")
    public String handleStageResolve(@RequestParam int currentPlayer, @RequestParam String input) {
        return game.handleStageResolve(currentPlayer, input);
    }

    @GetMapping("/getPlayerIndexInGame")
    public int getPlayerIndexInGame(@RequestParam String pname){
        int count = 0;
        for(Player p: game.getPlayers()) {
            if (!p.getName().equals(pname)) {
                count++;
            }else{
                return count;
            }
        }
        return 0;
    }

    @GetMapping("/getStageWinners")
    public List<Player> getStageWinners(){
        return game.getStageWinners();
    }


    @GetMapping("/endGame")
    public String endGame(){
        return game.endGame();
    }

    @GetMapping("/handleQuestEnd")
    public String handleQuestEnd(){
        return game.handleQuestEnd();
    }

    @GetMapping("/getEndQuest")
    public boolean getEndQuest(){return game.getEndQuest();}

    @GetMapping("/rigScenarios")
    public void rigScenarios(@RequestParam int scenario){
        game.rigScenarios(scenario);
    }

}
