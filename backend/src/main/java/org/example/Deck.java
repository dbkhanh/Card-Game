package org.example;

import java.util.*;

public class Deck {
    private List<Card> cards;
    private List<Card> discardPile;

    public Deck(String type) {
        this.cards = new ArrayList<>();
        this.discardPile = new ArrayList<>();
        if (type.equals("Adventure")) {
            generateAdventureCards();
        } else if (type.equals("Event")) {
            generateEventCards();
        }
    }

    private void generateAdventureCards() {
        //Add Foe Cards
        addFoeCards(5, 8);
        addFoeCards(10, 7);
        addFoeCards(15, 8);
        addFoeCards(20, 7);
        addFoeCards(25, 7);
        addFoeCards(30, 4);
        addFoeCards(35, 4);
        addFoeCards(40, 2);
        addFoeCards(50, 2);
        addFoeCards(70, 1);

        //Add Weapon cards
        addWeaponCards("D", 5, 6);
        addWeaponCards("H", 10, 12);
        addWeaponCards("S", 10, 16);
        addWeaponCards("B", 15, 8);
        addWeaponCards("L", 20, 6);
        addWeaponCards("E", 30, 2);
    }

    private void generateEventCards() {
        //Add Quest cards
        addEventCards("Q2", 3, 2);
        addEventCards("Q3", 4, 3);
        addEventCards("Q4", 3, 4);
        addEventCards("Q5", 2, 5);

        //Add Event cards
        cards.add(new Card("Plague", "Event", -2));
        cards.add(new Card("Queen's Favor", "Event", 2));
        cards.add(new Card("Queen's Favor", "Event",2 ));
        cards.add(new Card("Prosperity", "Event",2));
        cards.add(new Card("Prosperity", "Event", 2));
    }

    //Add Foe cards
    private void addFoeCards(int value, int count) {
        for (int i = 0; i < count; i++) {
            cards.add(new Card("F" + value, "Foe", value));
        }
    }

    //Add Weapon cards
    private void addWeaponCards(String prefix, int value, int count) {
        for (int i = 0; i < count; i++) {
            cards.add(new Card(prefix + value, "Weapon", value));
        }
    }

    public void stackCardsOnTop(List<Card> riggedCards) {
        cards.addAll(0, riggedCards);
    }

    //Add Event cards
    private void addEventCards(String name, int count, int value) {
        for (int i = 0; i < count; i++) {
            cards.add(new Card(name, "Event", value));
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            reshuffleDiscardPile();
        }
        if (!cards.isEmpty()) {
            return cards.remove(0);
        }
        return null;
    }

    public void reshuffleDiscardPile() {
        if (!discardPile.isEmpty()) {
            System.out.println("Reshuffling discard pile into the deck...");
            cards.addAll(discardPile);
            discardPile.clear();
            shuffle();
        }
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }
    public int getDeckSize(){
        return cards.size();
    }
    public void discard(Card card) {
        discardPile.add(card);
    }
}
