package io.quarkiverse.jdbc.singlestore.it.jpa;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * Various tests for the JPA integration.
 * WARNING: these tests will ONLY pass in native mode, as it also verifies reflection non-functionality.
 */
@Path("/jpa")
public class JPATestReflectionEndpoint {

    @GET
    @Path("/testreflection")
    public String doGet() throws IOException {
        StringBuilder resp = new StringBuilder();
        makeSureNonEntityAreDCE(resp);
        makeSureEntitiesAreAccessibleViaReflection(resp);
        makeSureNonAnnotatedEmbeddableAreAccessibleViaReflection(resp);
        makeSureAnnotatedEmbeddableAreAccessibleViaReflection(resp);
        String packageName = this.getClass().getPackage().getName();
        makeSureClassAreAccessibleViaReflection(packageName + ".Human", "Unable to enlist @MappedSuperclass", resp);
        makeSureClassAreAccessibleViaReflection(packageName + ".Animal", "Unable to enlist entity superclass", resp);
        return "OK";
    }

    private void makeSureClassAreAccessibleViaReflection(String className, String errorMessage, StringBuilder resp)
            throws IOException {
        try {
            className = getTrickedClassName(className);

            Class<?> custClass = Class.forName(className);
            Object instance = custClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            reportException(errorMessage, e, resp);
        }
    }

    private void makeSureEntitiesAreAccessibleViaReflection(StringBuilder resp) throws IOException {
        try {
            String className = getTrickedClassName(Customer.class.getName());

            Class<?> custClass = Class.forName(className);
            Object instance = custClass.getDeclaredConstructor().newInstance();
            Field id = custClass.getDeclaredField("id");
            id.setAccessible(true);
            if (id.get(instance) != null) {
                resp.append("id should be reachable and null");
            }
            Method setter = custClass.getDeclaredMethod("setName", String.class);
            Method getter = custClass.getDeclaredMethod("getName");
            setter.invoke(instance, "Emmanuel");
            if (!"Emmanuel".equals(getter.invoke(instance))) {
                resp.append("getter / setter should be reachable and usable");
            }
        } catch (Exception e) {
            reportException(e, resp);
        }
    }

    private void makeSureAnnotatedEmbeddableAreAccessibleViaReflection(StringBuilder resp) throws IOException {
        try {
            String className = getTrickedClassName(WorkAddress.class.getName());

            Class<?> custClass = Class.forName(className);
            Object instance = custClass.getDeclaredConstructor().newInstance();
            Method setter = custClass.getDeclaredMethod("setCompany", String.class);
            Method getter = custClass.getDeclaredMethod("getCompany");
            setter.invoke(instance, "Red Hat");
            if (!"Red Hat".equals(getter.invoke(instance))) {
                resp.append("@Embeddable embeddable should be reachable and usable");
            }
        } catch (Exception e) {
            reportException(e, resp);
        }
    }

    private void makeSureNonAnnotatedEmbeddableAreAccessibleViaReflection(StringBuilder resp) throws IOException {
        try {
            String className = getTrickedClassName(Address.class.getName());

            Class<?> custClass = Class.forName(className);
            Object instance = custClass.getDeclaredConstructor().newInstance();
            Method setter = custClass.getDeclaredMethod("setStreet1", String.class);
            Method getter = custClass.getDeclaredMethod("getStreet1");
            setter.invoke(instance, "1 rue du General Leclerc");
            if (!"1 rue du General Leclerc".equals(getter.invoke(instance))) {
                resp.append("Non @Embeddable embeddable getter / setter should be reachable and usable");
            }
        } catch (Exception e) {
            reportException(e, resp);
        }
    }

    private void makeSureNonEntityAreDCE(StringBuilder resp) {
        try {
            String className = getTrickedClassName(NotAnEntityNotReferenced.class.getName());

            Class<?> custClass = Class.forName(className);
            resp.append("Should not be able to find a non referenced non entity class");
            Object instance = custClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // Expected outcome
        }
    }

    /**
     * Trick SubstrateVM not to detect a simple use of Class.forname
     */
    private String getTrickedClassName(String className) {
        className = className + " ITrickYou";
        className = className.subSequence(0, className.indexOf(' ')).toString();
        return className;
    }

    private void reportException(final Exception e, final StringBuilder resp) throws IOException {
        reportException(null, e, resp);
    }

    private void reportException(String errorMessage, final Exception e, final StringBuilder resp) throws IOException {
        if (errorMessage != null) {
            resp.append(errorMessage);
            resp.append(" ");
        }
        resp.append(e.toString());
        resp.append("\n\t");
        //        e.printStackTrace(resp);
        resp.append("\n\t");
    }

}
