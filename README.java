    private YourClassName yourClassInstance;

    @Before
    public void setup() {
        yourClassInstance = Mockito.spy(new YourClassName());
    }

    @Test
    public void testGetAvailableStatus() {
        // Mock getRunningThreads()
        Stoppable mockStoppable1 = Mockito.mock(Stoppable.class);
        Stoppable mockStoppable2 = Mockito.mock(Stoppable.class);
        List<Stoppable> mockList = Arrays.asList(mockStoppable1, mockStoppable2);

        when(mockStoppable1.getType()).thenReturn(Stoppable.Type.YOUR_TYPE);
        when(mockStoppable1.getStatus()).thenReturn(AvailableStatus.STARTED);

        when(mockStoppable2.getType()).thenReturn(Stoppable.Type.YOUR_TYPE);
        when(mockStoppable2.getStatus()).thenReturn(AvailableStatus.STARTED);

        when(yourClassInstance.getRunningThreads()).thenReturn(mockList);

        // Test
        AvailableStatus result = yourClassInstance.getAvailableStatus(Stoppable.Type.YOUR_TYPE);
        assertEquals(AvailableStatus.STARTED, result);
    }
