package com.labs.stimaupdate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.local.UserIdStorageFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class OutageReportsFrag extends Fragment {
    ListView lvReportsList;
    ProgressDialog progressDialog;
    private OutageReportsListener outageReportsListener;

    public OutageReportsFrag() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_outage_reports, container, false);

        Toolbar outageReportsToolbar = view.findViewById(R.id.toolbarMyReports);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(outageReportsToolbar);
        outageReportsToolbar.setTitle("My Outage Reports");
        outageReportsToolbar.setNavigationIcon(R.drawable.ic_navigate_before_white_24dp);

        outageReportsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                outageReportsListener.backFromOutageReportsFrag();
            }
        });

        lvReportsList = view.findViewById(R.id.lvAllReports);
        lvReportsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String restoredDate = null;
                String createdDate;

                if (MainActivity.reports.get(i).getUpdated() != null) {
                    restoredDate = new SimpleDateFormat("MM-dd-yy").format(MainActivity.reports.get(i).getUpdated());
                    createdDate = new SimpleDateFormat("dd-MM-yy").format(MainActivity.reports.get(i).getCreated());
                } else {
                    createdDate = new SimpleDateFormat("dd-MM-yy").format(MainActivity.reports.get(i).getCreated());
                }


                ReportStatusFrag reportStatusFrag = new ReportStatusFrag();
                Bundle bundle = new Bundle();
                bundle.putInt("reportNumber", MainActivity.reports.get(i).getId());
                bundle.putString("address", MainActivity.reports.get(i).getAddress());
                bundle.putString("created", createdDate);
                bundle.putBoolean("technicianOnSIte", MainActivity.reports.get(i).isTechnicianOnSite());
                bundle.putBoolean("restored", MainActivity.reports.get(i).isRestored());
                bundle.putString("restoredDate", restoredDate);

                Log.d("LISTCHOICE :         ", restoredDate + "  " + createdDate);

                reportStatusFrag.setArguments(bundle);
                outageReportsListener.openReportStatusFrag(reportStatusFrag);
            }
        });
        outageReportsList();
        return view;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        outageReportsListener = (OutageReportsListener) activity;
    }

    private void outageReportsList() {
        progressDialog = ProgressDialog.show(getContext(), "Loading Reports...", null, true, true);

        String ownerID = UserIdStorageFactory.instance().getStorage().get();
        String whereClause = "ownerId = '" + ownerID + "'";

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();

        queryBuilder.addProperty("address");
        queryBuilder.addProperty("created ");
        queryBuilder.addProperty("updated ");
        queryBuilder.addProperty("technicianOnSite");
        queryBuilder.addProperty("restored");
        queryBuilder.addProperty("objectId");
        queryBuilder.addProperty("restoredDate");
        queryBuilder.addProperty("id");
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of(Report.class).find(queryBuilder, new AsyncCallback<List<Report>>() {
            @Override
            public void handleResponse(List<Report> response) {
                MainActivity.reports = response;
                MainActivity.reportAdapter = new ReportAdapter(getContext(), response);
                lvReportsList.setAdapter(MainActivity.reportAdapter);
                progressDialog.dismiss();
                // Log.d("REPORTS: ",response.get(1).getCreated().toString());

                Log.d("REPORTS:          ", String.valueOf(response.size()));
                Log.d("UPDATED:          ", String.valueOf(response.get(1).getCreated()));

                String formateDate = new SimpleDateFormat("MM-dd-yyyy").format(response.get(1).getCreated());


            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d("MAINACTIVITY FAULT: ", fault.getMessage());
                progressDialog.dismiss();
            }
        });

    }

    public String dateFormetter(String time) {
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        String outputPattern = "dd-MMM-yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public interface OutageReportsListener {
        void openReportStatusFrag(ReportStatusFrag i);

        void backFromOutageReportsFrag();
    }
}
