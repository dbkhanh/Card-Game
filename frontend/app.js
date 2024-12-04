// Base URL for API calls
const apiBaseUrl = "http://localhost:8080";
let playersToTrim = [];
let currentPlayerIndex = 0;
let currentSponsorIndex = 0;
let totalPlayers = 4;
let stageWinners = getStageWinners();

// Utility to log messages to the on-screen console
function logToConsole(message) {
    const consoleDiv = document.getElementById("console");
    const logEntry = document.createElement("div");
    logEntry.style.whiteSpace = "pre-wrap";
    logEntry.textContent = `${message}`;
    consoleDiv.appendChild(logEntry);
    consoleDiv.scrollTop = consoleDiv.scrollHeight;
}

async function setupGame() {
    try {
        const response = await fetch(`${apiBaseUrl}/setupGame`);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const result = await response.text();
        document.getElementById("console").innerText += result;

    } catch (error) {
        logToConsole(`Error starting game: ${error.message}`);
    }
}

async function playTurn() {
    try {
        const response = await fetch(`${apiBaseUrl}/playTurn`);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const result = await response.text();
        document.getElementById("console").innerText += result;

        questCompleted = false;
        if (result.includes('A quest card has been drawn')){
            await promptSponsor();
        }else{
            await startTrimming();
            checkPlayersToTrim();

            if (playersToTrim.length === 0) {
                setTimeout(playTurn, 1000);
            }
        }


    } catch (error) {
        logToConsole(`Error starting game: ${error.message}`);
    }
}

async function trimHandPlayer() {
    if (playersToTrim.length === 0) {
        logToConsole("No more players need to trim their hands.");
        return;
    }

    const playerIndex = playersToTrim[0];
    let playerHand = await getPlayerHand(playerIndex);
    getPlayerShield(playerIndex);
    let cardCount = playerHand.split(",").length;

    while (cardCount > 12) {
        const inputValue = document.getElementById("trim-card-index").value.trim();
        if (!inputValue) {
            return;
        }
        try {
            const response = await fetch(`${apiBaseUrl}/trimHandPlayer?input=${encodeURIComponent(inputValue)}&player=${playerIndex}`, { method: 'GET' });
            if (!response.ok) {
                throw new Error(`Failed to trim hand for player ${playerIndex}`);
            }

            const result = await response.text();
            document.getElementById("console").innerText += result;
            document.getElementById("trim-card-index").value = '';
            document.getElementById("trim-card-index").focus();

            playerHand = await getPlayerHand(playerIndex);
            getPlayerShield(playerIndex);
            cardCount = playerHand.split(",").length;

            if (cardCount <= 12) {
                logToConsole(`Player ${playerIndex + 1}'s hand has been trimmed to 12 cards.`);
            } else {
                logToConsole(`Player ${playerIndex + 1}, please discard more cards to trim your hand to 12.`);
            }
        } catch (error) {
            console.error('Error trimming hand for player:', error);
            logToConsole(`Error trimming hand: ${error.message}`);
            return;
        }
    }

    playersToTrim.shift();
}

async function promptSponsor() {
    try {
        let askedPlayers = 0;
        currentSponsorIndex = await getCurrentPlayerIndex();

        console.log(currentSponsorIndex);

        while (askedPlayers < totalPlayers) {
            logToConsole(`Player ${currentSponsorIndex + 1}, do you want to sponsor the quest? (y/n)`);

            // Wait for player input
            const input = await new Promise((resolve) => {
                const sponsorInput = document.getElementById("trim-card-index");
                getPlayerHand(currentSponsorIndex);
                getPlayerShield(currentSponsorIndex);

                sponsorInput.focus();

                const handleInput = (event) => {
                    if (event.key === "Enter") {
                        const value = sponsorInput.value.trim().toLowerCase();
                        sponsorInput.value = ""; // Clear input field
                        event.preventDefault();

                        if (value === "y" || value === "n") {
                            sponsorInput.removeEventListener("keypress", handleInput);
                            resolve(value);
                        } else {
                            logToConsole("Invalid input. Please enter 'y' or 'n'.");
                            sponsorInput.focus();
                        }
                    }
                };

                sponsorInput.addEventListener("keypress", handleInput);
            });

            // Send input to the backend
            try {
                const response = await fetch(
                    `${apiBaseUrl}/promptSponsor?input=${encodeURIComponent(input)}&currentIndex=${currentSponsorIndex}`,
                    { method: "GET" }
                );

                const result = await response.text();
                logToConsole(result);

                // If sponsorship is accepted, stop further prompts
                if (result.includes("will sponsor the quest")) {
                    //SPONSOR HERE
                    logToConsole(`Player ${currentSponsorIndex + 1} is building the quest!`);
                    await handleQuestSponsor();
                    return;
                }
            } catch (error) {
                console.error("Error processing sponsor input:", error);
                logToConsole(`Error processing sponsor input: ${error.message}`);
            }

            // Move to the next player in circular order
            currentSponsorIndex = (currentSponsorIndex + 1) % totalPlayers;
            askedPlayers++;
        }

        // If all players decline
        logToConsole("All players declined to sponsor the quest.");
        await setDoneStage(true);
        await nextPlayer();
        await playTurn();
    } catch (error) {
        console.error("Error in promptSponsor:", error);
        logToConsole(`Error in promptSponsor: ${error.message}`);
    }
}

async function setDoneStage(doneStageValue) {
    try {
        const response = await fetch(`${apiBaseUrl}/setDoneStage?doneStage=${doneStageValue}`, {
            method: "POST",
        });

        if (!response.ok) {
            throw new Error(`Failed to update doneStage. Status: ${response.status}`);
        }

        const result = await response.text();
    } catch (error) {
        console.error("Error updating doneStage:", error);
    }
}



// Function to get the player's hand
async function getPlayerHand(playerIndex) {
    try {
        const response = await fetch(`${apiBaseUrl}/getHand?player=${playerIndex}`);
        if (!response.ok) {
            throw new Error(`Failed to get hand for player ${playerIndex}`);
        }
        const hand = await response.text();
        console.log(`Player ${playerIndex + 1} hand:`, hand);
        document.getElementById('player-hand').innerText = `Player ${playerIndex + 1} Hand: ${hand}`;

        return hand;
    } catch (error) {
        console.error('Error fetching player hand:', error);
    }
}

async function getPlayerShield(playerIndex) {
    try {
        const response = await fetch(`${apiBaseUrl}/getPlayerShield?player=${playerIndex}`);
        if (!response.ok) {
            throw new Error(`Failed to get hand for player ${playerIndex}`);
        }
        const shield = await response.text();
        document.getElementById('players-shields').innerText = `Player ${playerIndex + 1} Shield: ${shield}`;

        return shield;
    } catch (error) {
        console.error('Error fetching player hand:', error);
    }
}

async function getDoneStage() {
    try {
        const response = await fetch(`${apiBaseUrl}/getDoneStage`);
        if (!response.ok) {
            throw new Error(`Failed to get doneStage status`);
        }
        const doneStage = await response.text();
        return doneStage === "true";
    } catch (error) {
        console.error('Error fetching player hand:', error);
    }
}

async function nextPlayer() {
    try {
        const response = await fetch(`${apiBaseUrl}/nextPlayer`);
        if (!response.ok) {
            throw new Error(`Failed to do next player`);
        }
        const result = await response.text();
        return result;
    } catch (error) {
        console.error('Error fetching player hand:', error);
    }
}

async function drawCardsForParticipants() {
    try {
        const response = await fetch(`${apiBaseUrl}/drawCardsForParticipants`);

        if (!response.ok) {
            throw new Error(`Failed to do next player`);
        }
        const result = await response.text();
        logToConsole(result);
        return result;
    } catch (error) {
        console.error('Error fetching player hand:', error);
    }
}

async function getCurrentPlayerIndex() {
    try {
        const response = await fetch(`${apiBaseUrl}/getCurrentPlayerIndex`);
        if (!response.ok) {
            throw new Error(`Failed to get sponsor Player's index.`);
        }
        const index = await response.text();
        console.log(`Current player index: ${index}`);
        return parseInt(index, 10);
    } catch (error) {
        console.error('Error fetching player name:', error);
    }
}

async function getShield() {
    try {
        const response = await fetch(`${apiBaseUrl}/getShield`);
        if (!response.ok) {
            throw new Error(`Failed to get current Player's shield`);
        }
        const shield = await response.text();
        console.log(`Current player shield: ${shield}`);
        return parseInt(shield, 10);
    } catch (error) {
        console.error('Error fetching player shield:', error);
    }
}


async function getNewDrawnCard() {
    try {
        const response = await fetch(`${apiBaseUrl}/getNewDrawnCard`);
        if (!response.ok) {
            throw new Error(`Failed to get getNewDrawnCard value`);
        }
        const value = await response.text();
        console.log(`getNewDrawnCard value: ${value}`);
        return parseInt(value, 10);
    } catch (error) {
        console.error('Error fetching getNewDrawnCard:', error);
    }
}

// This function checks which players need to trim their hands
async function checkPlayersToTrim() {
    try {
        const response = await fetch(`${apiBaseUrl}/checkPlayerHands`);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        playersToTrim = await response.json();  // Update the list of players who need to trim
    } catch (error) {
        console.error('Error checking players to trim hands:', error);
    }
}

async function getStageWinners() {
    try {
        const response = await fetch(`${apiBaseUrl}/getStageWinners`);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        console.log(`Currents stageWinners: ${stageWinners}`);
        return await response.json();
    } catch (error) {
        console.error('Get stage winners', error);
    }
}

async function startTrimming() {
    await checkPlayersToTrim();

    if (playersToTrim.length === 0) {
        logToConsole("No players need to trim their hands.");
        return Promise.resolve();
    }

    return new Promise((resolve) => {
        const trimInput = document.getElementById("trim-card-index");
        trimInput.value = "";
        const handleTrim = async (event) => {
            if (event.key === "Enter") {
                event.preventDefault();
                console.log(trimInput.value);
                await trimHandPlayer();
                trimInput.value = ''; // Clear input field

                if (playersToTrim.length === 0) {
                    logToConsole("All players have trimmed their hands.");
                    trimInput.removeEventListener("keypress", handleTrim);
                    resolve(); // Signal that trimming is complete
                } else {
                    logToConsole(`Now it's Player ${playersToTrim[0] + 1}'s turn to trim. Type the position of the card to trim:
`);
                    await getPlayerHand(playersToTrim[0]);
                    await getPlayerShield(playersToTrim[0]);
                }
            }
        };

        trimInput.removeEventListener("keypress", handleTrim);
        trimInput.addEventListener("keypress", handleTrim);
        trimInput.focus();
        trimInput.value = "";
        logToConsole(`It's Player ${playersToTrim[0] + 1}'s turn to trim their hand. Type the position of the card to trim: `);
        getPlayerHand(playersToTrim[0]);
        getPlayerShield(playersToTrim[0]);
    });
}

async function handleQuestSponsor() {
    const inputField = document.getElementById("trim-card-index");
    let currentStage = 1;
    const totalStages = await getNewDrawnCard();
    let stageValid = false;

    inputField.focus();

    const handleInput = async (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            const inputValue = inputField.value.trim();

            if (!inputValue) {
                logToConsole("Input is required to proceed with quest sponsorship.");
                inputField.focus();
                return;
            }

            try {
                const response = await fetch(
                    `${apiBaseUrl}/handleQuestSponsor?input=${encodeURIComponent(inputValue)}&stageNumber=${currentStage}`,
                    { method: "GET" }
                );

                if (!response.ok) {
                    throw new Error(`Failed to handle quest sponsor: ${response.status}`);
                }

                const result = await response.text();
                logToConsole(result);

                if (result.includes("Warning:")) {
                    // Stage is invalid, stay in the current stage
                    stageValid = false;
                } else if (inputValue.toLowerCase() === "quit") {
                    if (result.includes("Stage setup complete")) {
                        stageValid = true;
                        getPlayerHand(currentSponsorIndex);
                        getPlayerShield(currentSponsorIndex);
                        // Proceed to the next stage
                        currentStage++;
                        if (currentStage > totalStages) {
                            logToConsole("\nQuest setup process finished.\n\n");
                            inputField.removeEventListener("keypress", handleInput);
                            getPlayerHand(currentSponsorIndex );
                            getPlayerShield(currentSponsorIndex);
                            handlePlayerParticipation();
                            return;
                        }
                        logToConsole(`Proceeding to stage ${currentStage}. Enter the next card.`);
                    } else {
                        logToConsole(`Stage ${currentStage} setup is still invalid. Please add more cards.`);
                    }
                } else if (result.includes("added to the stage.")){
                    logToConsole("Card added. Continue adding cards or type 'quit' to finish the stage.");
                }
            } catch (error) {
                console.error("Error handling quest sponsor:", error);
                logToConsole(`Error handling quest sponsor: ${error.message}`);
            } finally {
                inputField.value = ""; // Clear input field
                inputField.focus(); // Refocus for the next input
            }
        }
    };

    inputField.removeEventListener("keypress", handleInput); // Prevent duplicate listeners
    inputField.addEventListener("keypress", handleInput);

    logToConsole(`Quest sponsorship setup started. Enter the position of the card to add for stage ${currentStage}. Type 'quit' to finish the stage.`);
}

async function handlePlayerParticipation() {
    const inputField = document.getElementById("trim-card-index");
    const sponsorIndex = await getCurrentSponsorIndex(); // Fetch sponsor index from the backend
    let phase = "participation";
    let currentPlayerIndex = 0; // Start with the first player
    let currentStageIndex = 1;

    inputField.focus();

    const handleInput = async (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            const inputValue = inputField.value.trim();

            if (!inputValue) {
                logToConsole("Input is required to proceed.");
                inputField.focus();
                return;
            }

            try {
                const response = await fetch(
                    `${apiBaseUrl}/handlePlayerParticipation?currentPlayerIndex=${currentPlayerIndex}&input=${encodeURIComponent(inputValue)}`,
                    { method: "GET" }
                );

                if (!response.ok) {
                    throw new Error(`Failed to handle player participation: ${response.status}`);
                }

                const result = await response.text();
                logToConsole(result);

                if (phase === "participation") {
                    // Skip the sponsor
                    do {
                        currentPlayerIndex++;
                    } while (currentPlayerIndex === sponsorIndex);

                    if (currentPlayerIndex >= totalPlayers) {
                        phase = "stages";
                        currentStageIndex = 1;
                    } else {
                        logToConsole("\n\n" + `Player ${currentPlayerIndex + 1}, do you want to participate in this stage? (y/n)`);
                        getPlayerHand(currentPlayerIndex);
                        getPlayerShield(currentPlayerIndex);
                    }
                } else if (phase === "stages" && result.includes("Stage resolved")) {
                    currentStageIndex++;
                    logToConsole(`Proceeding to stage ${currentStageIndex}. Enter input for the next stage.`);
                } else if (phase === "stages" && result.includes("Quest process finished.")) {
                    logToConsole("Quest process finished.");
                    inputField.removeEventListener("keypress", handleInput);
                    return;
                }

                if(result.includes("draws 1 adventure card.")){
                    inputField.removeEventListener("keypress", handleInput);
                    document.getElementById("trim-card-index").value = '';
                    await startTrimming();
                    await handlePlayerAttack();
                }

                if(result.includes("for sponsoring the quest")){
                    inputField.removeEventListener("keypress", handleInput);
                    document.getElementById("trim-card-index").value = '';
                    await startTrimming();
                    await nextPlayer();
                    await playTurn();
                    return;
                }
            } catch (error) {
                console.error("Error handling player participation:", error);
                logToConsole(`Error: ${error.message}`);
            } finally {
                inputField.value = "";
                inputField.focus();
            }
        }
    };

    inputField.removeEventListener("keypress", handleInput); // Prevent duplicate listeners
    inputField.addEventListener("keypress", handleInput);

    // Start by skipping the sponsor if the first player is the sponsor
    if (currentPlayerIndex === sponsorIndex) {
        currentPlayerIndex++;
    }

    logToConsole(`Player ${currentPlayerIndex + 1}, do you want to participate in this stage? (y/n)`);
    getPlayerHand(currentPlayerIndex);
    getPlayerShield(currentPlayerIndex);
}


// Helper function to get the sponsor index from the backend
async function getCurrentSponsorIndex() {
    try {
        const response = await fetch(`${apiBaseUrl}/getCurrentSponsor`);
        if (!response.ok) {
            throw new Error(`Failed to fetch sponsor index: ${response.status}`);
        }
        return parseInt(await response.text(), 10);
    } catch (error) {
        console.error("Error fetching sponsor index:", error);
        throw error;
    }
}

async function getParticipants() {
    try {
        const response = await fetch(`${apiBaseUrl}/getParticipants`);
        if (!response.ok) {
            throw new Error(`Failed to fetch participants: ${response.status}`);
        }
        const participants = await response.json();
        logToConsole("Current Participants:");
        participants.forEach((participant, index) => {
            logToConsole(`${index + 1}. ${participant.name} (Shields: ${participant.shields})`);
        });
        return participants;
    } catch (error) {
        console.error("Error fetching participants:", error);
        logToConsole(`Error fetching participants: ${error.message}`);
        throw error;
    }
}

async function handlePlayerAttack() {
    const inputField = document.getElementById("trim-card-index");
    let stageParticipants = await getParticipants();
    let currentParticipantIndex = 0;

    if (!stageParticipants || stageParticipants.length === 0) {
        logToConsole("No participants available for this stage.");
        return;
    }

    inputField.focus();

    const handleInput = async (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            const inputValue = inputField.value.trim();

            if (!inputValue) {
                logToConsole("Input is required to proceed. this is handle attack!");
                inputField.focus();
                return;
            }

            try {
                if (currentParticipantIndex >= stageParticipants.length) {
                    inputField.removeEventListener("keypress", handleInput);
                    return;
                }

                const currentParticipant = stageParticipants[currentParticipantIndex];
                if (!currentParticipant || !currentParticipant.name) {
                    logToConsole("Invalid participant data.");
                    inputField.removeEventListener("keypress", handleInput);
                    return;
                }

                const currentPlayerIndex = await getPlayerIndexInGame(currentParticipant.name);

                const attackResponse = await fetch(
                    `${apiBaseUrl}/handlePlayerAttack?currentPlayer=${currentPlayerIndex}&input=${encodeURIComponent(inputValue)}`,
                    { method: "GET" }
                );

                if (!attackResponse.ok) {
                    throw new Error(`Failed to handle player attack: ${attackResponse.status}`);
                }

                const attackResult = await attackResponse.text();
                logToConsole(attackResult);

                if (inputValue.toLowerCase() === "quit") {
                    currentParticipantIndex++;

                    if (currentParticipantIndex >= stageParticipants.length) {
                        stageWinners = await getStageWinners();
                        inputField.removeEventListener("keypress", handleInput);
                        await resolveStage();
                        return;
                    }

                    const nextParticipant = stageParticipants[currentParticipantIndex];
                    if (!nextParticipant) {
                        logToConsole("No participants available for this stage.");
                        return;
                    }
                    const nextParticipantIndex = await getPlayerIndexInGame(nextParticipant.name);
                    getPlayerHand(nextParticipantIndex);
                    getPlayerShield(nextParticipantIndex);
                    logToConsole(`${nextParticipant.name}'s turn. Enter the position of your next attack card or 'quit' to end selecting.`);
                } else {
                    logToConsole(`${currentParticipant.name}, you can continue adding attack cards or type 'quit' to end your turn.`);
                }
            } catch (error) {
                console.error("Error handling player attack:", error);
                logToConsole(`Error: ${error.message}`);
            } finally {
                inputField.value = "";
                inputField.focus();
            }
        }
    };

    inputField.removeEventListener("keypress", handleInput);
    inputField.addEventListener("keypress", handleInput);

    const firstParticipant = stageParticipants[currentParticipantIndex];
    if (!firstParticipant) {
        logToConsole("No participants available for this stage.");
        return;
    }

    const firstParticipantIndex = await getPlayerIndexInGame(firstParticipant.name);
    getPlayerHand(firstParticipantIndex);
    getPlayerShield(firstParticipantIndex);
    logToConsole(`${firstParticipant.name}'s turn. Enter the position of your next attack card or 'quit' to end selecting.`);
}

async function doneQuest(){
    const final = await endGame();
    if(final.includes("End Game!")) return;
    questCompleted = true;
    await startTrimming();
    await nextPlayer();
    await playTurn();
}

let questCompleted = false;
async function resolveStage() {
    const inputField = document.getElementById("trim-card-index");
    let stageWinners = await getStageWinners();
    let currentWinnerIndex = 0;
    let currentStageIndex = 1;
    const totalStages = await getNewDrawnCard();
    inputField.focus();

    if ((!stageWinners || stageWinners.length === 0) && (questCompleted === false)) {
        logToConsole("No stage winners to resolve.");
        inputField.removeEventListener("keypress", handleInput); // Ensure cleanup
        await handleQuestEnd();
        await doneQuest();
        return;
    }

    async function handleInput(event) {
        if (event.key === "Enter") {
            event.preventDefault();
            const inputValue = inputField.value.trim();

            if (!inputValue) {
                logToConsole("Input is required to proceed.");
                inputField.focus();
                return;
            }

            try {
                const currentWinner = stageWinners[currentWinnerIndex];
                if (!currentWinner || !currentWinner.name) {
                    logToConsole("Invalid winner data.");
                    inputField.removeEventListener("keypress", handleInput);
                    return;
                }

                // Fetch from the backend and handle the response
                const currentWinnerIndexInGame = await getPlayerIndexInGame(currentWinner.name);
                const response = await fetch(`${apiBaseUrl}/handleStageResolve?currentPlayer=${currentWinnerIndexInGame}&input=${encodeURIComponent(inputValue)}`, {
                    method: "GET"
                });


                if (!response.ok) {
                    throw new Error(`Failed to resolve stage for player ${currentWinner.name}: ${response.status}`);
                }

                const responseText = await response.text();
                logToConsole(responseText); // Output the result from the backend

                if (responseText.includes("The quest has failed")) {
                    inputField.removeEventListener("keypress", handleInput);
                    en
                    await doneQuest();
                    return;
                }

                // Move to the next participant
                currentWinnerIndex++;
                inputField.value = "";

                if (currentWinnerIndex >= stageWinners.length) {
                    currentWinnerIndex = 0;
                    currentStageIndex++;

                    if (currentStageIndex > totalStages) {
                        logToConsole("The quest has ended.");
                        logToConsole("I am here!")
                        inputField.removeEventListener("keypress", handleInput);
                        await doneQuest();
                        return;
                    }

                    // Fetch new stage winners for the next stage
                    stageWinners = await getStageWinners();
                    if (!stageWinners || stageWinners.length === 0) {
                        logToConsole("No participants remain. The quest has failed.");
                        inputField.removeEventListener("keypress", handleInput);
                        await doneQuest();
                        return;
                    }


                    inputField.removeEventListener("keypress", handleInput);
                    await drawCardsForParticipants();
                    await startTrimming();
                    logToConsole(`Proceeding to next stage. Eligible participants will now attack.`);
                    await handlePlayerAttack();
                    return;
                }

                // Prompt the next player
                const nextWinner = stageWinners[currentWinnerIndex];
                logToConsole(`${nextWinner.name}, do you want to continue for the next stage? (y/n)`);
            } catch (error) {
                console.error("Error resolving stage:", error);
                logToConsole(`Error: ${error.message}`);
            } finally {
                inputField.focus();
            }
        }
    }

    inputField.removeEventListener("keypress", handleInput);
    inputField.addEventListener("keypress", handleInput);

    // Start prompting winners for the current stage
    const firstWinner = stageWinners[currentWinnerIndex];
    logToConsole(`${firstWinner.name}, do you want to continue for the next stage? (y/n)`);
}


async function getPlayerIndexInGame(playerName) {
    try {
        const response = await fetch(`${apiBaseUrl}/getPlayerIndexInGame?pname=${encodeURIComponent(playerName)}`);
        if (!response.ok) {
            throw new Error(`Failed to fetch player index: ${response.status}`);
        }

        const playerIndex = await response.text();
        return parseInt(playerIndex, 10); // Convert the response to an integer
    } catch (error) {
        console.error("Error fetching player index:", error);
        logToConsole(`Error fetching player index: ${error.message}`);
        throw error; // Rethrow error to handle it in calling code
    }
}

async function endGame(){
    try {
        const response = await fetch(`${apiBaseUrl}/endGame`);
        if (!response.ok) {
            throw new Error(`Failed to fetch player index: ${response.status}`);
        }
        const result = await response.text();
        logToConsole(result);
        return result;
    } catch (error) {
        console.error("Error fetching end Game:", error);
        logToConsole(`Error fetching player index: ${error.message}`);
        throw error;
    }
}

async function handleQuestEnd(){
    try {
        const response = await fetch(`${apiBaseUrl}/handleQuestEnd`);
        if (!response.ok) {
            throw new Error(`Failed to fetch player index: ${response.status}`);
        }
        const result = await response.text();
        logToConsole(result);
        return result;
    } catch (error) {
        console.error("Error fetching handleQuestEnd:", error);
        logToConsole(`Error fetching player index: ${error.message}`);
        throw error;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("test-btn").addEventListener("click", setupGame);
    document.getElementById("play-turn-btn").addEventListener("click", playTurn);
});