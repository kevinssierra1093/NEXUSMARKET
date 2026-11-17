package application.domain.valueobjects;

public final class SystemRole extends DomainCatalog {

    public static final SystemRole BUYER = new SystemRole(
            "BUYER", "Buyer", "Person who purchases products published on the platform.");
    public static final SystemRole SELLER = new SystemRole(
            "SELLER", "Seller", "Person or business responsible for registering and managing products.");
    public static final SystemRole LOGISTICS_OPERATOR = new SystemRole(
            "LOGISTICS_OPERATOR", "Logistics Operator", "Responsible for the physical operation of warehouses and dispatches.");
    public static final SystemRole ADMINISTRATOR = new SystemRole(
            "ADMINISTRATOR", "Administrator", "Responsible for administering sellers and warehouses.");
    public static final SystemRole SUPERVISOR = new SystemRole(
            "SUPERVISOR", "Supervisor", "Read-only profile for operational monitoring and consultation.");

    private SystemRole(String code, String name, String description) {
        super(code, name, description);
    }
}
