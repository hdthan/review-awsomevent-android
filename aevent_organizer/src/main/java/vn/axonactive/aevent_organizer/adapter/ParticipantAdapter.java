package vn.axonactive.aevent_organizer.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import vn.axonactive.aevent_organizer.R;
import vn.axonactive.aevent_organizer.model.Participant;

/**
 * Created by dtnhat on 1/17/2017.
 */

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ParticipantHolder> {

    private Context context;
    private List<Participant> mParticipants;
    private long eventId;

    public ParticipantAdapter(List<Participant> participants, long eventId) {
        this.mParticipants = participants;
        this.eventId = eventId;
    }

    @Override
    public ParticipantAdapter.ParticipantHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        context = parent.getContext();
        View inflatedView = LayoutInflater.from(context)
                .inflate(R.layout.participant_row, parent, false);
        return new ParticipantHolder(context, inflatedView);
    }

    @Override
    public void onBindViewHolder(ParticipantAdapter.ParticipantHolder holder, int position) {
        Participant enrollment = mParticipants.get(position);
        holder.bindParticipant(enrollment);
    }

    @Override
    public int getItemCount() {
        return mParticipants.size();
    }

    public void setFilter(List<Participant> participants) {

        mParticipants = new ArrayList<>();
        mParticipants.addAll(participants);

        notifyDataSetChanged();

    }

    class ParticipantHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Context context;

        private TextView mName;
        private TextView mEmail;
        private TextView mPhone;
        private CheckBox mCheck;
        private Participant mParticipant;
        private DatabaseReference eventRef;

        ParticipantHolder(Context context, View v) {
            super(v);
            this.context = context;
            mName = (TextView) v.findViewById(R.id.userName);
            mEmail = (TextView) v.findViewById(R.id.email);
            mPhone = (TextView) v.findViewById(R.id.phone);
            mCheck = (CheckBox) v.findViewById(R.id.checkBox);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            eventRef = database.getReference("events").child(eventId + "");
            mCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int value = mCheck.isChecked() ? 1 : 0;

                    if (value == 0) {
                        confirm();
                    } else {
                        eventRef.child(mParticipant.getUserId() + "").child("check").setValue(value);
                    }
                }
            });

            v.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

        }

        private void confirm() {

            String message = "Are you sure you want to uncheck?";

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(message)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            eventRef.child(mParticipant.getUserId() + "").child("check").setValue(0);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mCheck.setChecked(true);
                        }
                    });
            Dialog dialog = builder.create();
            dialog.show();
        }


        void bindParticipant(Participant participant) {
            mParticipant = participant;
            mName.setText(mParticipant.getFullName());
            mEmail.setText(mParticipant.getEmail());
            mPhone.setText(mParticipant.getPhone());
            mCheck.setChecked(mParticipant.getCheck() == 1);
        }

    }
}
