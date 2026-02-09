# Tournament of Kings - Java Card Game 

Tournament of Kings is a turn-based card game implemented in Java, where players participate in quests, defeat foes using weapons, and earn shields by completing stages. The primary goal of this project was **not gameplay complexity**, but **software correctness, testability, and quality assurance**.

This project was developed as part of a **Quality Assurance (QA) course** with a strong emphasis on automated testing and behavior-driven development.

---

## 🎮 Game Rules

- Players take turns drawing cards and resolving their effects.
- The objective is to be the first player to reach **7 shields**.

### Quests
- A quest consists of a fixed number of stages defined by a quest card.
- One player sponsors the quest and constructs each stage using:
  - Exactly **one foe card**
  - Optional weapon cards (no duplicates per stage)
- Each stage must have a **strictly higher total strength** than the previous stage.

### Quest Participation
- Non-sponsoring players may choose to participate or withdraw.
- Participating players build attacks using weapon cards.
- Players whose attack strength is insufficient are eliminated from the quest.
- Remaining players continue until the quest ends or all participants are eliminated.

### Rewards & Events
- Successful players earn shields equal to the number of stages completed.
- Sponsors draw cards after the quest based on cards used and quest length.
- Event cards apply immediate effects such as shield loss or additional card draws.


## 🛠️ Tools & Technologies

### Core Technologies
- **Java** – Core game logic and object-oriented design
- **Maven / Gradle** – Dependency and build management (if applicable)

### Testing & QA
- **JUnit** – Unit testing of core game rules, state transitions, and edge cases
- **Cucumber** – Behavior-Driven Development (BDD) using Gherkin feature files to validate game scenarios end-to-end
- **Selenium WebDriver** – Automated UI and workflow testing to simulate real user interactions

### Testing Focus
- Rule enforcement and validation
- Quest stage construction and progression
- Card draw, discard, and deck integrity
- Player elimination and shield assignment
- Multi-player game flow and edge cases

---

## 🧠 Engineering Concepts Demonstrated

- Object-Oriented Design (OOP)
- Test-Driven & Behavior-Driven Development (TDD / BDD)
- Automated test design and maintenance
- Deterministic testing with controlled game states
- Separation of game logic and test logic

---

## 📌 Summary

This project demonstrates the ability to design **testable systems**, implement **robust automated test suites**, and validate complex rule-based logic using industry-standard QA tools.

