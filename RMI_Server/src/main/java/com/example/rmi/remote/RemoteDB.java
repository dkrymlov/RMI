package com.example.rmi.remote;

import com.example.rmi.component.TableData;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import com.example.rmi.component.*;
import com.example.rmi.component.column.ColumnType;

public interface RemoteDB extends Remote {

  public List<Row> getRows(int tableIndex) throws RemoteException;

  public List<Column> getColumns(int tableIndex) throws RemoteException;

  public List<TableData> getTablesData() throws RemoteException;



  public Boolean createTable(String name) throws RemoteException;

  public Boolean addRow(int tableIndex) throws RemoteException;

  public Boolean addColumn(int tableIndex, String name, ColumnType columnType, String min, String max) throws RemoteException;



  public Boolean deleteTable(int tableIndex) throws RemoteException;

  public Boolean deleteColumn(int tableIndex, int columnIndex) throws RemoteException;

  public Boolean deleteRow(int tableIndex, int rowIndex) throws RemoteException;



  public Boolean renameTable(int tableIndex, String name) throws RemoteException;
  public Boolean renameColumn(int tableIndex, int columnIndex, String name) throws RemoteException;
  public Boolean changeColumnType(int tableIndex, int columnIndex, ColumnType columnType) throws RemoteException;



  public Boolean editCell(int tableIndex, int rowIndex, int columnIndex, String value) throws RemoteException;
  public void createTestTable() throws RemoteException;
  public List<Row> projection(int tableIndex, int column, String operator, String inputValue) throws RemoteException;

  }

