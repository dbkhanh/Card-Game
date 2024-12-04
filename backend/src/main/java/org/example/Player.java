package org.example;

import java.io.PrintWriter;
import java.util.*;

public class Player {
    private String name;
    private List<Card> hand;
    private int shields;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.shields = 0;  // Starting number of shields
    }

    public String getName() {
        return name;
    }
    public void drawCards(Deck deck, int count) {
        for (int i = 0; i < count; i++) {
            Card card = deck.draw();
            if (card != null) {
                hand.add(card);
            }
        }
    }

    //Sort adventure cards within player's hand and show the hand after
    public String showHand() {
        hand.sort((c1, c2) -> {
            if (!c1.getType().equals(c2.getType())) {
                return c1.getType().equals("Foe") ? -1 : 1;
            }

            int valueCompare = Integer.compare(c1.getValue(), c2.getValue());
            if (valueCompare != 0) {
                return valueCompare;
            }
            return c1.getName().startsWith("S") ? -1 : 1;
        });

        return hand.stream()
                .map(Card::toString)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    public String trimHand(String input) {
        StringBuilder result = new StringBuilder();

        // Check if the hand size is greater than 12 cards
        if (hand.size() > 12) {
            // Convert the input string to an integer
            int position = -1;
            try {
                position = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                result.append("\n\nInvalid input. Please enter a valid number.\n\n");
                return result.toString();  // Return immediately if input is invalid
            }

            if (position >= 1 && position <= hand.size()) {
                // Discard the selected card
                Card cardToDiscard = hand.remove(position - 1);
                result.append("\n\nYou discarded: ").append(cardToDiscard).append("\n");
            } else {
                result.append("\nInvalid position. Please enter a number between 1 and ")
                        .append(hand.size()).append(".\n");
            }
        }

        // When the hand is reduced to 12 or fewer cards
        result.append(getName()).append(" has ").append(showHand()).append("\n");
        result.append("Your hand is now trimmed to ").append(hand.size()).append(" cards.\n");
//        if(hand.size() > 12) result.append(promptForTrimHand());

        return result.toString();
    }





    public void setHand(List<Card> newHand) {
        this.hand = new ArrayList<>(newHand);
    }

    public void discardAllCards() {
        this.hand.clear();
    }

    public void loseShields(int amount) {
        shields -= amount;
        if (shields < 0) shields = 0;
    }

    public void addShields(int amount) {
        shields += amount;
    }

    public int getShields() {
        return shields;
    }

    @Override
    public String toString() {
        return name + " (Shields: " + shields + ")";
    }

    public int getHandSize() {
        return hand.size();
    }

    public List<Card> getHand(){
        return hand;
    }

    public void initializeDefaultHand() {
        // Define the default cards
        hand.clear();
        hand.add(new Card("F5", "Foe", 5));
        hand.add(new Card("F5", "Foe", 5));
        hand.add(new Card("F15", "Foe", 15));
        hand.add(new Card("F20", "Foe", 20));
        hand.add(new Card("S10", "Weapon", 10));
        hand.add(new Card("B15", "Weapon", 15));
        hand.add(new Card("L20", "Weapon", 20));
    }
}
