package com.example.rmi.controller;

import com.example.rmi.component.Column;
import com.example.rmi.component.OperatorType;
import com.example.rmi.component.Row;
import com.example.rmi.component.TableData;
import com.example.rmi.component.column.ColumnType;
import com.example.rmi.component.column.MoneyInvlColumn;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

import static com.example.rmi.RmiClientApplication.remoteDB;

@Controller
public class DatabaseController {

    @GetMapping("/")
    public String index(Model model) throws RemoteException {
        model.addAttribute("tables", remoteDB.getTablesData());
        return "index";
    }

    @GetMapping("/viewTable")
    public String viewTable(@Valid int tableIndex, Model model) throws RemoteException {
        model.addAttribute("tables", remoteDB.getTablesData());
        model.addAttribute("thisTable", remoteDB.getTablesData().get(tableIndex));
        model.addAttribute("columns", remoteDB.getColumns(tableIndex));
        OperatorType[] operatorTypes = OperatorType.values();
        List<String> types = new ArrayList<>();
        for (int i = 0; i < operatorTypes.length; i++) {
            types.add(operatorTypes[i].getValue());
        }
        model.addAttribute("types",types);
        // Assuming tableIndex 0 is your test table
        List<Row> rows = remoteDB.getRows(tableIndex);
        System.out.println(rows.size());
        model.addAttribute("rows", rows);
        return "viewTable";
    }

    @GetMapping("/addTable")
    public String addTable(Model model) throws RemoteException {
        model.addAttribute("tables", remoteDB.getTablesData());
        return "addTable";
    }

    @PostMapping("/addTable")
    public String addTable(@Valid String name, Model model) throws RemoteException {
        remoteDB.createTable(name);
        return "redirect:/viewTable?tableIndex=" + String.valueOf(remoteDB.getTablesData().size()-1); // Redirect to the view table page
    }

    @GetMapping("/addColumn")
    public String addColumn(Model model, @RequestParam Map<String, String> allParams) throws RemoteException {
        model.addAttribute("tableIndex",allParams.get("tableIndex"));
        ColumnType[] columnTypes = ColumnType.values();
        List<String> types = new ArrayList<>();
        for (int i = 0; i < columnTypes.length; i++) {
            types.add(columnTypes[i].name());
        }
        model.addAttribute("types",types);
        return "addColumn";
    }

    @PostMapping("/addColumn")
    public String addColumn(@Valid String name, @Valid ColumnType columnType, @Valid String min, @Valid String max, Model model, @RequestParam Map<String, String> allParams) throws RemoteException {
        int tableIndex = Integer.parseInt(allParams.get("tableIndex"));
        remoteDB.addColumn(tableIndex, name, columnType,min,max);
        return "redirect:/viewTable?tableIndex=" + String.valueOf(tableIndex); // Redirect to the view table page
    }

    @PostMapping("/addRow")
    public String addRow(
        @RequestParam Map<String, String> allParams,
        Model model, HttpServletRequest request) throws RemoteException {
        String referer = request.getHeader("Referer");
        int tableIndex = Integer.parseInt(allParams.get("tableIndex"));
        System.out.println(remoteDB.addRow(tableIndex));
        return "redirect:" + referer; // Redirect to the view table page
    }

    @Transactional
    @PostMapping("/deleteRow")
    public String deleteRow(
        @RequestParam Map<String, String> allParams,
        Model model, HttpServletRequest request) throws RemoteException {
        String referer = request.getHeader("Referer");
        int tableIndex = Integer.parseInt(allParams.get("tableIndex"));
        int rowIndex = Integer.parseInt(allParams.get("rowIndex"));
        System.out.println(remoteDB.deleteRow(tableIndex,rowIndex));
        return "redirect:" + referer; // Redirect to the view table page
    }

    @Transactional
    @PostMapping("/deleteColumn")
    public String deleteColumn(
        @RequestParam Map<String, String> allParams,
        Model model, HttpServletRequest request) throws RemoteException {
        String referer = request.getHeader("Referer");
        int tableIndex = Integer.parseInt(allParams.get("tableIndex"));
        int columnIndex = Integer.parseInt(allParams.get("columnIndex"));
        System.out.println(remoteDB.deleteColumn(tableIndex,columnIndex));
        return "redirect:" + referer; // Redirect to the view table page
    }

    @Transactional
    @PostMapping("/deleteTable")
    public String deleteTable(
        @RequestParam Map<String, String> allParams,
        Model model, HttpServletRequest request) throws RemoteException {
        String referer = request.getHeader("Referer");
        int tableIndex = Integer.parseInt(allParams.get("tableIndex"));
        System.out.println(remoteDB.deleteTable(tableIndex));
        return "redirect:/"; // Redirect to the view table page
    }

    @PostMapping("/editCell")
    public String editCell(
            @RequestParam Map<String, String> allParams,
            Model model, HttpServletRequest request) throws RemoteException {
        String referer = request.getHeader("Referer");


        // Extracting rowIndex and columnIndex
        int rowIndex = Integer.parseInt(allParams.get("rowIndex"));
        int columnIndex = Integer.parseInt(allParams.get("columnIndex"));
        int tableIndex = Integer.parseInt(allParams.get("tableIndex"));

        // Extracting the dynamic value parameter
        String newValue = allParams.get("value-" + rowIndex + "-" + columnIndex);

        System.out.println(newValue);

        // Check if newValue is present and not empty
        if (newValue != null && !newValue.trim().isEmpty()) {
            // Perform the edit operation
            System.out.println(remoteDB.editCell(tableIndex, rowIndex, columnIndex, newValue));
        }

        return "redirect:" + referer; // Redirect to the view table page
    }

    @PostMapping("/projection")
    public String projection(@Valid String value, @Valid String columnProjectionIndex, @Valid String operatorType,
                             @RequestParam Map<String, String> allParams, Model model) throws RemoteException {
        int tableIndex = Integer.parseInt(allParams.get("tableIndex"));
        int columnIndex = Integer.parseInt(columnProjectionIndex);
        List<Column> columns = remoteDB.getColumns(tableIndex);
        List<Row> newRows= remoteDB.projection(tableIndex,columnIndex, operatorType,value);
        List<TableData> tableData = remoteDB.getTablesData();
        remoteDB.createTable(tableData.get(tableIndex).name+"_projection");
        for (int i = 0; i < columns.size();i++){
            System.out.println(i);
            if (ColumnType.valueOf(columns.get(i).type) == ColumnType.MONEY_INVL) {
                MoneyInvlColumn column = (MoneyInvlColumn) columns.get(i);
                remoteDB.addColumn(tableData.size(), column.name, ColumnType.valueOf(column.type),
                        column.getMin(), column.getMax());
            }
            else {
                remoteDB.addColumn(tableData.size(), columns.get(i).name, ColumnType.valueOf(columns.get(i).type),
                        "", "");
            }
        }
        for (int i = 0; i < newRows.size(); i++) {
            remoteDB.addRow(tableData.size());
            for (int i1 = 0; i1 < newRows.get(i).values.size(); i1++) {
                remoteDB.editCell(tableData.size(),i,i1,newRows.get(i).values.get(i1));
            }
        }
        return "redirect:/viewTable?tableIndex=" + String.valueOf(remoteDB.getTablesData().size()-1); // Redirect to the view table page
    }

    // Other mappings...

}
