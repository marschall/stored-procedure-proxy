
module com.github.marschall.stored.procedure.proxy {

  exports com.github.marschall.storedprocedureproxy;
  exports com.github.marschall.storedprocedureproxy.annotations;
  exports com.github.marschall.storedprocedureproxy.spi;

  requires static spring.beans;
  requires static spring.core;
  requires static spring.jdbc;
  requires static spring.tx;
  requires static org.postgresql.jdbc;

}
