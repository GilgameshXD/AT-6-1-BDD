package test;

import data.DataHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import page.DashboardPage;
import page.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static data.DataHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoneyTransferTest {
    DashboardPage dashboardPage;
    DataHelper.CardInfo firstCardInfo;
    DataHelper.CardInfo secondCardInfo;
    int firstCardBalance;
    int secondCardBalance;

    @BeforeEach
    void setupForTests() {
        var loginPage = open("http://localhost:9999", LoginPage.class);
        var authInfo = getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = getVerificationCode();
        dashboardPage = verificationPage.validVerify(verificationCode);
        firstCardInfo = getFirstCardInfo();
        secondCardInfo = getSecondCardInfo();
        firstCardBalance = dashboardPage.getCardBalance(firstCardInfo);
        secondCardBalance = dashboardPage.getCardBalance(secondCardInfo);
    }

    @Test
    void shouldTransferFromFirstToSecond() {
        var amount = generateValidAmount(firstCardBalance);
        var expectedBalanceOnFirstCard = firstCardBalance - amount;
        var expectedBalanceOnSecondCard = secondCardBalance + amount;
        var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo);
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardInfo);
        var actualBalanceOnFirstCard = dashboardPage.getCardBalance(firstCardInfo);
        var actualBalanceOnSecondCard = dashboardPage.getCardBalance(secondCardInfo);
        assertEquals(expectedBalanceOnFirstCard, actualBalanceOnFirstCard);
        assertEquals(expectedBalanceOnSecondCard, actualBalanceOnSecondCard);
    }

    @Test
    void shouldTransferFromSecondToFirst() {
        var amount = generateValidAmount(firstCardBalance);
        var expectedBalanceOnFirstCard = firstCardBalance + amount;
        var expectedBalanceOnSecondCard = secondCardBalance - amount;
        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), secondCardInfo);
        var actualBalanceOnFirstCard = dashboardPage.getCardBalance(firstCardInfo);
        var actualBalanceOnSecondCard = dashboardPage.getCardBalance(secondCardInfo);
        assertEquals(expectedBalanceOnFirstCard, actualBalanceOnFirstCard);
        assertEquals(expectedBalanceOnSecondCard, actualBalanceOnSecondCard);
    }

    @Test
    void shouldShowErrorMessage() {
        var amount = generateInvalidAmount(secondCardBalance);
        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        transferPage.makeTransfer(String.valueOf(amount), secondCardInfo);
        transferPage.findErrorMessage("Невозможно совершить перевод. На счёте недостаточно средств.");
        var actualBalanceOnFirstCard = dashboardPage.getCardBalance(firstCardInfo);
        var actualBalanceOnSecondCard = dashboardPage.getCardBalance(secondCardInfo);
        assertEquals(firstCardBalance, actualBalanceOnFirstCard);
        assertEquals(secondCardBalance, actualBalanceOnSecondCard);
    }

}
