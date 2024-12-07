const { Builder, By, until } = require('selenium-webdriver');

async function runTest() {
    let driver = await new Builder().forBrowser('chrome').build();

    try {
        await driver.get('http://127.0.0.1:8081');

        let setUpButton = await driver.findElement(By.xpath("//button[contains(text(), 'Setup Game')]"));
        await setUpButton.click();
        await driver.wait(until.elementTextContains(driver.findElement(By.id('console')), '--- Shuffling decks ---'), 3000);
        console.log("Game setup successfully.");

        let scenario2Rig = await driver.findElement(By.xpath("//button[contains(text(), 'Scenario 2')]"));
        await scenario2Rig.click();
        await driver.wait(until.elementTextContains(driver.findElement(By.id('console')), 'Scenario 2 rigged successfully!'), 3000);
        console.log("Rigged cards for scenario 2 successfully.");

        let startGame = await driver.findElement(By.xpath("//button[contains(text(), 'Start Game')]"));
        await startGame.click();
        await driver.wait(until.elementTextContains(driver.findElement(By.id('console')), '--- Turn of Player 1 ---'), 5000);
        console.log("Game started successfully.");

        let input = "y\n1\nquit\n1\n6\nquit\n1\n5\nquit\n1\n5\nquit\n" +
                    "y\ny\ny\n1\n1\n1\n6\nquit\nquit\n6\nquit\n" +
                    "y\ny\n4\nquit\n4\nquit\ny\ny\n6\n7\nquit\n" +
                    "6\n7\nquit\ny\ny\n6\n7\nquit\n6\n7\nquit\n" +
                    "1\n1\n1\n1\n" +
                    "n\ny\n1\nquit\n1\n4\nquit\n1\n5\nquit\n" +
                    "n\ny\ny\n6\nquit\n6\nquit\ny\ny\n7\nquit\n" +
                    "7\nquit\ny\ny\n10\nquit\n10\nquit\n2\n2\n2\n";

        let inputField = await driver.findElement(By.id('trim-card-index'));
        for (const char of input) {
            if (char === '\n') {
                await inputField.sendKeys('\n');
                await driver.sleep(500);
            } else {
                await inputField.sendKeys(char);
            }
        }

        let expectedFinalStates = [
            `Player 1's final hand after quest is F15, F15, F20, F20, F20, F20, F25, F25, F30, H10, B15, L20\nPlayer 1's final shield after quest is 0`,
            `Player 2's final hand after quest is F10, F15, F15, F25, F30, F40, F50, L20, L20\nPlayer 2's final shield after quest is 7`,
            `Player 3's final hand after quest is F20, F40, D5, D5, S10, H10, H10, H10, H10, B15, B15, L20\nPlayer 3's final shield after quest is 0`,
            `Player 4's final hand after quest is F15, F15, F20, F25, F30, F50, F70, L20, L20\nPlayer 4's final shield after quest is 7`
        ];

        let consoleStatus = await driver.findElement(By.id('console')).getText();
        console.log("Final console status:", consoleStatus);

        for (let expectedState of expectedFinalStates) {
            console.assert(
                consoleStatus.includes(expectedState),
                `Test failed: Expected state not found in console.\nMissing: ${expectedState}`
            );

            if (consoleStatus.includes(expectedState)) {
                console.log(`\n\nVerified final state in console: ${expectedState}`);
            }
        }

        let winnerDeclared = "End Game!\n" +
                             "We have 2 winner(s):\n" +
                             "Winner: Player 2 with 7 shields!\n" +
                             "Winner: Player 4 with 7 shields!"

        console.assert(consoleStatus.includes(winnerDeclared),`Test failed: Error declaring the winners of the game`)
        if (consoleStatus.includes(winnerDeclared)) {
            console.log(`\n\nVerified final state in console: ${winnerDeclared}`);
        }

        console.log("\n\nAll final states verified successfully.\n\n");

    } catch (error) {
        console.error("Test encountered an error:", error);
    } finally {
        await driver.quit();
    }
}

runTest();
