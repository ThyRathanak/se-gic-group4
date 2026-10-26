package edu.itc.shop.service;

class WelcomeServiceTest {

    // Fake Notifier that records calls
    static class FakeNotifier implements Notifier {
        String lastTo;
        String lastText;

        @Override
        public void send(String to, String text) {
            this.lastTo = to;
            this.lastText = text;
        }
    }

    // @Test
    // void testWelcomeMessage() {
    // FakeNotifier defaultNotifier = new FakeNotifier();
    // FakeNotifier urgentNotifier = new FakeNotifier();

    // // Fixed clock without named arguments
    // Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T09:15:02Z"),
    // ZoneId.of("UTC"));

    // WelcomeService service = new WelcomeService(defaultNotifier, urgentNotifier,
    // fixedClock);

    // // Method call without named arguments
    // String result = service.welcome("ann", false);

    // assertEquals("Welcome ann! It is 09:15:02", result);
    // assertEquals("ann", defaultNotifier.lastTo);
    // assertEquals("Welcome ann! It is 09:15:02", defaultNotifier.lastText);
    // }
}