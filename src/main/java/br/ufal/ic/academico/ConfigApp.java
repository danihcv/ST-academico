package br.ufal.ic.academico;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Willy
 */
@Getter
@Setter
public class ConfigApp extends Configuration {
    
    private String university;
    private String state;
    private int port;
    
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();
}