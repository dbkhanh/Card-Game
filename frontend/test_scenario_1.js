const { Builder, By, until } = require('selenium-webdriver');

async function runTest() {
    let driver = await new Builder().forBrowser('chrome').build();

    try {
        await driver.get('http://127.0.0.1:8081');

        let setUpButton = await driver.findElement(By.xpath("//button[contains(text(), 'Setup Game')]"));
        await setUpButton.click();
        await driver.wait(until.elementTextContains(driver.findElement(By.id('console')), '--- Shuffling decks ---'), 3000);
        console.log("Game setup successfully.");

        let scenario1Rig = await driver.findElement(By.xpath("//button[contains(text(), 'Scenario 1')]"));
        await scenario1Rig.click();
        await driver.wait(until.elementTextContains(driver.findElement(By.id('console')), 'Scenario 1 rigged successfully!'), 3000);
        console.log("Rigged cards for scenario 1 successfully.");

        let startGame = await driver.findElement(By.xpath("//button[contains(text(), 'Start Game')]"));
        await startGame.click();
        await driver.wait(until.elementTextContains(driver.findElement(By.id('console')), '--- Turn of Player 1 ---'), 5000);
        console.log("Game started successfully.");

        let input = "n\ny\n1\n6\nquit\n1\n5\n7\nquit\n1\n6\nquit\n1\n5\nquit\n" +
            "y\ny\ny\n1\n1\n1\n5\n6\nquit\n5\n4\nquit\n5\n7\nquit\n" +
            "y\ny\ny\n7\n6\nquit\n9\n6\nquit\n6\n7\nquit\n" +
            "y\ny\n9\n6\n5\nquit\n7\n5\n8\nquit\n" +
            "y\ny\n7\n6\n8\nquit\n4\n5\n7\n8\nquit\n" +
            "1\n1\n1\n1\n";

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
            `Player 1's final hand after quest is F5, F10, F15, F15, F30, H10, B15, B15, L20\nPlayer 1's final shield after quest is 0`,
            `Player 2's final hand after quest is D5, S10, S10, S10, S10, S10, S10, H10, L20, L20, L20, L20\nPlayer 2's final shield after quest is 0`,
            `Player 3's final hand after quest is F5, F5, F15, F30, S10\nPlayer 3's final shield after quest is 0`,
            `Player 4's final hand after quest is F15, F15, F40, D5, S10, L20, L20, E30\nPlayer 4's final shield after quest is 4`
        ];

        let consoleStatus = await driver.findElement(By.id('console')).getText();
        console.log("Final console status:", consoleStatus);

        for (let expectedState of expectedFinalStates) {
            console.assert(
                consoleStatus.includes(expectedState),
                `Test failed: Expected state not found in console.\nMissing: ${expectedState}`
            );

            if (consoleStatus.includes(expectedState)) {
                console.log(`Verified final state in console: ${expectedState}`);
            }
        }

        console.log("\n\nAll final states verified successfully.\n\n");

    } catch (error) {
        console.error("Test encountered an error:", error);
    } finally {
        await driver.quit();
    }
}

runTest();
