package dessertj.tests.java21;

public class ConstantDynamicSample {
    private static int instanceCount = 0;

    public ConstantDynamicSample() {
        System.out.println("Creating ConstantDynamicSample instance...");
        instanceCount++;
    }

    public static int getInstanceCount() {
        return instanceCount;
    }
}
