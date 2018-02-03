package info.nightscout.androidaps.plugins.PumpCombo.ruffyscripter.commands;

import org.monkey.d.ruffy.ruffy.driver.display.MenuAttribute;
import org.monkey.d.ruffy.ruffy.driver.display.MenuType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.nightscout.androidaps.plugins.PumpCombo.ruffyscripter.history.Bolus;
import info.nightscout.androidaps.plugins.PumpCombo.ruffyscripter.history.PumpHistory;

public class ReadQuickInfoCommand extends BaseCommand {
    private static final Logger log = LoggerFactory.getLogger(ReadQuickInfoCommand.class);

    private final int numberOfBolusRecordsToRetrieve;

    public ReadQuickInfoCommand(int numberOfBolusRecordsToRetrieve) {
        this.numberOfBolusRecordsToRetrieve = numberOfBolusRecordsToRetrieve;
    }

    @Override
    public void execute() {
        scripter.verifyRootMenuIsDisplayed();
        // navigate to reservoir menu
        scripter.pressCheckKey();
        scripter.waitForMenuToBeLeft(MenuType.MAIN_MENU);
        scripter.waitForMenuToBeLeft(MenuType.STOP);
        scripter.verifyMenuIsDisplayed(MenuType.QUICK_INFO);
        result.reservoirLevel = ((Double) scripter.getCurrentMenu().getAttribute(MenuAttribute.REMAINING_INSULIN)).intValue();
        if (numberOfBolusRecordsToRetrieve > 0) {
            // navigate to bolus data menu
            scripter.pressCheckKey();
            scripter.verifyMenuIsDisplayed(MenuType.BOLUS_DATA);
            List<Bolus> bolusHistory = new ArrayList<>(numberOfBolusRecordsToRetrieve);
            result.history = new PumpHistory().bolusHistory(bolusHistory);
            for(int recordsLeftToRead = numberOfBolusRecordsToRetrieve; recordsLeftToRead > 0; recordsLeftToRead--) {
                scripter.verifyMenuIsDisplayed(MenuType.BOLUS_DATA);
                bolusHistory.add(readBolusRecord());
                int record = (int) scripter.getCurrentMenu().getAttribute(MenuAttribute.CURRENT_RECORD);
                scripter.pressDownKey();
                while (record == (int) scripter.getCurrentMenu().getAttribute(MenuAttribute.CURRENT_RECORD)) {
                    scripter.waitForScreenUpdate();
                }
            }
            if (log.isDebugEnabled()) {
                if (!result.history.bolusHistory.isEmpty()) {
                    log.debug("Read bolus history (" + result.history.bolusHistory.size() + "):");
                    for (Bolus bolus : result.history.bolusHistory) {
                        log.debug(new Date(bolus.timestamp) + ": " + bolus.toString());
                    }
                }
            }
        }
        scripter.returnToRootMenu();
        result.success = true;
    }

    @Override
    public boolean needsRunMode() {
        return false;
    }

    @Override
    public String toString() {
        return "ReadQuickInfoCommand{}";
    }
}
