
const { Builder, By, until } = require('selenium-webdriver');

async function runTest() {
    let driver = await new Builder().forBrowser('chrome').build();

    try {
        await driver.get('http://127.0.0.1:8081');

        let setUpButton = await driver.findElement(By.xpath("//button[contains(text(), 'Setup Game')]"));
        await setUpButton.click();
        await driver.wait(until.elementTextContains(driver.findElement(By.id('console')), '--- Shuffling decks ---'), 3000);
        console.log("Game setup successfully.");

        let scenario4Rig = await driver.findElement(By.xpath("//button[contains(text(), 'Scenario 4')]"));
        await scenario4Rig.click();
        await driver.wait(until.elementTextContains(driver.findElement(By.id('console')), 'Scenario 4 rigged successfully!'), 3000);
        console.log("Rigged cards for scenario 4 successfully.");

        let startGame = await driver.findElement(By.xpath("//button[contains(text(), 'Start Game')]"));
        await startGame.click();
        await driver.wait(until.elementTextContains(driver.findElement(By.id('console')), '--- Turn of Player 1 ---'), 5000);
        console.log("Game start successfully.");


        let input = "y\n1\n4\n5\n7\n9\n11\nquit\n1\n2\n3\n4\n5\n6\nquit\n" +
                    "y\ny\ny\n1\n4\n3\n12\nquit\nquit\nquit\n1\n1\n";

        let inputField = await driver.findElement(By.id('trim-card-index'));
        for (const char of input) {
            if (char === '\n') {
                await inputField.sendKeys('\n');
                await driver.sleep(1000);
            } else {
                await inputField.sendKeys(char);
            }
        }

        let consoleStatus = await driver.findElement(By.id('console')).getText();
        console.log("Final console status:", consoleStatus);

        if (consoleStatus.includes("No stage winners to resolve."))
            console.log("Test passed: Final game result is displayed correctly.");
    } catch (error) {
        console.error("Test encountered an error:", error);
    }
}

runTest();