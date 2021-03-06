package com.jsql.model.injection;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.jsql.exception.PreparationException;
import com.jsql.exception.StoppableException;
import com.jsql.model.vendor.DB2Strategy;
import com.jsql.model.vendor.FirebirdStrategy;
import com.jsql.model.vendor.InformixStrategy;
import com.jsql.model.vendor.IngresStrategy;
import com.jsql.model.vendor.MSSQLServerStrategy;
import com.jsql.model.vendor.MaxDbStrategy;
import com.jsql.model.vendor.MySQLStrategy;
import com.jsql.model.vendor.OracleStrategy;
import com.jsql.model.vendor.PostgreSQLStrategy;
import com.jsql.model.vendor.SybaseStrategy;

/**
 * Runnable class, define insertionCharacter that will be used by all futures requests,
 * i.e -1 in "[...].php?id=-1 union select[...]", sometimes it's -1, 0', 0, etc,
 * this class/function tries to find the working one by searching a special error message
 * in the source page.
 */
public class StoppableGetSQLVendor extends AbstractSuspendable {
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getLogger(StoppableGetSQLVendor.class);

    @Override
    public String action(Object... args) throws PreparationException, StoppableException {

        // Parallelize the search and let the user stops the process if needed.
        // SQL: force a wrong ORDER BY clause with an inexistent column, order by 1337,
        // and check if a correct error message is sent back by the server:
        //         Unknown column '1337' in 'order clause'
        // or   supplied argument is not a valid MySQL result resource
        ExecutorService taskExecutor = Executors.newCachedThreadPool();
        CompletionService<CallableSourceCode> taskCompletionService = new ExecutorCompletionService<CallableSourceCode>(taskExecutor);
        for (String insertionCharacter : new String[] {"'\"#-)'\""}) {
            taskCompletionService.submit(
                new CallableSourceCode(
                    insertionCharacter,
                    insertionCharacter
                )
            );
        }

        int total = 1;
        while (0 < total) {
            // The user need to stop the job
            if (this.stopOrPause()) {
                throw new StoppableException();
            }
            try {
                CallableSourceCode currentCallable = taskCompletionService.take().get();
                total--;
                String pageSource = currentCallable.getContent();
                
                if (Pattern.compile(".*MySQL.*", Pattern.DOTALL).matcher(pageSource).matches()) {
                    MediatorModel.model().sqlStrategy = new MySQLStrategy();
                    System.out.println("MySQLStrategy");
                }
                if (Pattern.compile(".*function\\.pg.*", Pattern.DOTALL).matcher(pageSource).matches()) {
                    MediatorModel.model().sqlStrategy = new PostgreSQLStrategy();
                    System.out.println("PostgreSQLStrategy");
                }
                if (Pattern.compile(".*function\\.oci.*", Pattern.DOTALL).matcher(pageSource).matches()) {
                    MediatorModel.model().sqlStrategy = new OracleStrategy();
                    System.out.println("OracleStrategy");
                }
                if (Pattern.compile(".*SQL Server.*", Pattern.DOTALL).matcher(pageSource).matches()) {
                    MediatorModel.model().sqlStrategy = new MSSQLServerStrategy();
                    System.out.println("SQLServerStrategy");
                }
                if (Pattern.compile(".*function\\.db2.*", Pattern.DOTALL).matcher(pageSource).matches()) {
                    MediatorModel.model().sqlStrategy = new DB2Strategy();
                    System.out.println("DB2Strategy");
                }
                if (Pattern.compile(".*Non-terminated string.*", Pattern.DOTALL).matcher(pageSource).matches()) {
                    MediatorModel.model().sqlStrategy = new IngresStrategy();
                    System.out.println("IngresStrategy");
                }
                if (Pattern.compile(".*function\\.sybase.*", Pattern.DOTALL).matcher(pageSource).matches()) {
                    MediatorModel.model().sqlStrategy = new SybaseStrategy();
                    System.out.println("SybaseStrategy");
                }
                if (Pattern.compile(".*maxdb\\.query.*", Pattern.DOTALL).matcher(pageSource).matches()) {
                    MediatorModel.model().sqlStrategy = new MaxDbStrategy();
                    System.out.println("MaxDbStrategy");
                }
                if (Pattern.compile(".*Informix.*", Pattern.DOTALL).matcher(pageSource).matches()) {
                    MediatorModel.model().sqlStrategy = new InformixStrategy();
                    System.out.println("InformixStrategy");
                }
                if (Pattern.compile(".*function\\.ibase-query.*", Pattern.DOTALL).matcher(pageSource).matches()) {
                    MediatorModel.model().sqlStrategy = new FirebirdStrategy();
                    System.out.println("FirebirdStrategy");
                }
            } catch (InterruptedException e) {
                LOGGER.error(e, e);
            } catch (ExecutionException e) {
                LOGGER.error(e, e);
            }
        }
        return null;
    }
}