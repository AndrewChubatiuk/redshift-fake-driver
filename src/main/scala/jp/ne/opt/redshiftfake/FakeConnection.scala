package jp.ne.opt.redshiftfake

import java.sql.{Array => _, _}
import java.{util => jutil}
import java.util.Properties
import java.util.concurrent.Executor

import jp.ne.opt.redshiftfake.FakePreparedStatement.{FakeUnloadPreparedStatement, FakeAsIsPreparedStatement, FakeCopyPreparedStatement}
import jp.ne.opt.redshiftfake.parse.{DDLParser, UnloadCommandParser, CopyCommandParser}
import jp.ne.opt.redshiftfake.s3.S3Service

class FakeConnection(underlying: Connection, s3Service: S3Service) extends Connection {
  private[this] val dummyQuery = "select 1 as one"

  //========================
  // Intercept Statement
  //========================
  def createStatement(): Statement = new FakeStatement(
    underlying.createStatement(), underlying, StatementType.Plain, s3Service)
  def createStatement(resultSetType: Int, resultSetConcurrency: Int): Statement =
    new FakeStatement(underlying.createStatement(), underlying,
      StatementType.ResultSetTypeConcurrency(resultSetType, resultSetConcurrency), s3Service)
  def createStatement(resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): Statement =
    new FakeStatement(underlying.createStatement(), underlying,
      StatementType.ResultSetTypeConcurrencyHoldability(resultSetType, resultSetConcurrency, resultSetHoldability), s3Service)

  //========================
  // Intercept PreparedStatement
  //========================
  def prepareStatement(sql: String): PreparedStatement = {
    val sanitized = DDLParser.sanitize(sql)

    CopyCommandParser.parse(sanitized).map { command =>
      new FakeCopyPreparedStatement(
        underlying.prepareStatement(dummyQuery), command, underlying, PreparedStatementType.Plain, s3Service)
    }.orElse(UnloadCommandParser.parse(sanitized).map { command =>
      new FakeUnloadPreparedStatement(
        underlying.prepareStatement(dummyQuery), command, underlying, PreparedStatementType.Plain, s3Service)
    }).getOrElse(new FakeAsIsPreparedStatement(underlying.prepareStatement(sanitized)))
  }
  def prepareStatement(sql: String, columnNames: Array[String]): PreparedStatement = {
    val sanitized = DDLParser.sanitize(sql)

    CopyCommandParser.parse(sanitized).map { command =>
      new FakeCopyPreparedStatement(
        underlying.prepareStatement(dummyQuery), command, underlying, PreparedStatementType.ColumnNames(columnNames), s3Service)
    }.orElse(UnloadCommandParser.parse(sanitized).map { command =>
      new FakeUnloadPreparedStatement(
        underlying.prepareStatement(dummyQuery), command, underlying, PreparedStatementType.ColumnNames(columnNames), s3Service)
    }).getOrElse(new FakeAsIsPreparedStatement(underlying.prepareStatement(sanitized, columnNames)))
  }
  def prepareStatement(sql: String, resultSetType: Int, resultSetConcurrency: Int): PreparedStatement = {
    val sanitized = DDLParser.sanitize(sql)

    CopyCommandParser.parse(sanitized).map { command =>
      new FakeCopyPreparedStatement(
        underlying.prepareStatement(dummyQuery), command, underlying,
        PreparedStatementType.ResultSetTypeConcurrency(resultSetType, resultSetConcurrency), s3Service)
    }.orElse(UnloadCommandParser.parse(sanitized).map { command =>
      new FakeUnloadPreparedStatement(
        underlying.prepareStatement(dummyQuery), command, underlying,
        PreparedStatementType.ResultSetTypeConcurrency(resultSetType, resultSetConcurrency), s3Service)
    }).getOrElse(new FakeAsIsPreparedStatement(underlying.prepareStatement(sanitized, resultSetType, resultSetConcurrency)))
  }
  def prepareStatement(sql: String, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): PreparedStatement = {
    val sanitized = DDLParser.sanitize(sql)

    CopyCommandParser.parse(sanitized).map { command =>
      new FakeCopyPreparedStatement(underlying.prepareStatement(dummyQuery), command, underlying,
        PreparedStatementType.ResultSetTypeConcurrencyHoldability(resultSetType, resultSetConcurrency, resultSetHoldability), s3Service)
    }.orElse(UnloadCommandParser.parse(sanitized).map { command =>
      new FakeUnloadPreparedStatement(underlying.prepareStatement(dummyQuery), command, underlying,
        PreparedStatementType.ResultSetTypeConcurrencyHoldability(resultSetType, resultSetConcurrency, resultSetHoldability), s3Service)
    }).getOrElse(new FakeAsIsPreparedStatement(underlying.prepareStatement(sanitized, resultSetType, resultSetConcurrency, resultSetHoldability)))
  }
  def prepareStatement(sql: String, autoGeneratedKeys: Int): PreparedStatement = {
    val sanitized = DDLParser.sanitize(sql)

    CopyCommandParser.parse(sanitized).map { command =>
      new FakeCopyPreparedStatement(underlying.prepareStatement(dummyQuery), command, underlying,
        PreparedStatementType.AutoGeneratedKeys(autoGeneratedKeys), s3Service)
    }.orElse(UnloadCommandParser.parse(sanitized).map { command =>
      new FakeUnloadPreparedStatement(underlying.prepareStatement(dummyQuery), command, underlying,
        PreparedStatementType.AutoGeneratedKeys(autoGeneratedKeys), s3Service)
    }).getOrElse(new FakeAsIsPreparedStatement(underlying.prepareStatement(sanitized, autoGeneratedKeys)))
  }
  def prepareStatement(sql: String, columnIndexes: Array[Int]): PreparedStatement = {
    val sanitized = DDLParser.sanitize(sql)

    CopyCommandParser.parse(sanitized).map { command =>
      new FakeCopyPreparedStatement(underlying.prepareStatement(dummyQuery), command, underlying,
        PreparedStatementType.ColumnIndexes(columnIndexes), s3Service)
    }.orElse(UnloadCommandParser.parse(sanitized).map { command =>
      new FakeUnloadPreparedStatement(underlying.prepareStatement(dummyQuery), command, underlying,
        PreparedStatementType.ColumnIndexes(columnIndexes), s3Service)
    }).getOrElse(new FakeAsIsPreparedStatement(underlying.prepareStatement(sanitized, columnIndexes)))
  }

  //========================
  // Just delegate to underlying
  //========================
  def setAutoCommit(autoCommit: Boolean): Unit = underlying.setAutoCommit(autoCommit)
  def setHoldability(holdability: Int): Unit = underlying.setHoldability(holdability)
  def clearWarnings(): Unit = underlying.clearWarnings()
  def getNetworkTimeout: Int = underlying.getNetworkTimeout
  def createBlob(): Blob = underlying.createBlob()
  def createSQLXML(): SQLXML = underlying.createSQLXML()
  def setSavepoint(): Savepoint = underlying.setSavepoint()
  def setSavepoint(name: String): Savepoint = underlying.setSavepoint(name)
  def createNClob(): NClob = underlying.createNClob()
  def getTransactionIsolation: Int = underlying.getTransactionIsolation
  def getClientInfo(name: String): String = underlying.getClientInfo(name)
  def getClientInfo: Properties = underlying.getClientInfo
  def getSchema: String = underlying.getSchema
  def setNetworkTimeout(executor: Executor, milliseconds: Int): Unit = underlying.setNetworkTimeout(executor, milliseconds)
  def getMetaData: DatabaseMetaData = underlying.getMetaData
  def getTypeMap: jutil.Map[String, Class[_]] = underlying.getTypeMap
  def rollback(): Unit = underlying.rollback()
  def rollback(savepoint: Savepoint): Unit = underlying.rollback(savepoint)
  def getHoldability: Int = underlying.getHoldability
  def setReadOnly(readOnly: Boolean): Unit = underlying.setReadOnly(readOnly)
  def setClientInfo(name: String, value: String): Unit = underlying.setClientInfo(name, value)
  def setClientInfo(properties: Properties): Unit = underlying.setClientInfo(properties)
  def isReadOnly: Boolean = underlying.isReadOnly
  def setTypeMap(map: jutil.Map[String, Class[_]]): Unit = underlying.setTypeMap(map)
  def getCatalog: String = underlying.getCatalog
  def createClob(): Clob = underlying.createClob()
  def nativeSQL(sql: String): String = underlying.nativeSQL(sql)
  def setTransactionIsolation(level: Int): Unit = underlying.setTransactionIsolation(level)
  def createArrayOf(typeName: String, elements: Array[AnyRef]): java.sql.Array = underlying.createArrayOf(typeName, elements)
  def setCatalog(catalog: String): Unit = underlying.setCatalog(catalog)
  def close(): Unit = underlying.close()
  def getAutoCommit: Boolean = underlying.getAutoCommit
  def abort(executor: Executor): Unit = underlying.abort(executor)
  def isValid(timeout: Int): Boolean = underlying.isValid(timeout)
  def releaseSavepoint(savepoint: Savepoint): Unit = underlying.releaseSavepoint(savepoint)
  def isClosed: Boolean = underlying.isClosed
  def createStruct(typeName: String, attributes: Array[AnyRef]): Struct = underlying.createStruct(typeName, attributes)
  def getWarnings: SQLWarning = underlying.getWarnings
  def setSchema(schema: String): Unit = underlying.setSchema(schema)
  def commit(): Unit = underlying.commit()
  def unwrap[T](iface: Class[T]): T = underlying.unwrap(iface)
  def isWrapperFor(iface: Class[_]): Boolean = underlying.isWrapperFor(iface)
  def prepareCall(sql: String): CallableStatement = underlying.prepareCall(sql)
  def prepareCall(sql: String, resultSetType: Int, resultSetConcurrency: Int): CallableStatement =
    underlying.prepareCall(sql, resultSetType, resultSetConcurrency)
  def prepareCall(sql: String, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): CallableStatement =
    underlying.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability)
}
