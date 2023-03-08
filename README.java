 @Test
    public void testMainMethod() throws Exception {
        // GIVEN
        String[] args = {"arg1", "arg2"};
        Manager managerMock = Mockito.mock(Manager.class);
        Mockito.doNothing().when(managerMock).exec(args);
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // WHEN
        Manager.main(args);

        // THEN
        String expectedOutput = "ICO_STOPPED";
        assertEquals(expectedOutput, outContent.toString().trim());
        Mockito.verify(managerMock).exec(args);
    }
