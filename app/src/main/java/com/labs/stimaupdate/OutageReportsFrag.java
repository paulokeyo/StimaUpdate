package com.labs.stimaupdate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                ReportStatusFrag reportStatusFrag = new ReportStatusFrag();
                Bundle bundle = new Bundle();
                bundle.putInt("reportNumber", MainActivity.reportStatuses.get(i).getReportNumber());
                bundle.putString("address", MainActivity.reportStatuses.get(i).getAddress());
                bundle.putString("outageReceivedDate", MainActivity.reportStatuses.get(i).getReportReceivedDate());
                bundle.putInt("technicianOnSIte", MainActivity.reportStatuses.get(i).getTechnicianOnSite());
                bundle.putInt("restored", MainActivity.reportStatuses.get(i).getRestored());

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
        String email = MainActivity.prefConfig.readEmail();

        Call<List<ReportStatus>> call = MainActivity.apiInterface.getOutageReports(email);
        call.enqueue(new Callback<List<ReportStatus>>() {
            @Override
            public void onResponse(Call<List<ReportStatus>> call, Response<List<ReportStatus>> response) {
                MainActivity.reportStatuses = response.body();
                MainActivity.reportStatusAdapter = new ReportStatusAdapter(getContext(), MainActivity.reportStatuses);
                lvReportsList.setAdapter(MainActivity.reportStatusAdapter);
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<List<ReportStatus>> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    public interface OutageReportsListener {
        void openReportStatusFrag(ReportStatusFrag i);

        void backFromOutageReportsFrag();
    }
}
