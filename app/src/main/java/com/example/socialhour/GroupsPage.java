package com.example.socialhour;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.services.DBConnection;
import com.example.services.TestClass;
import com.example.services.TimeConverter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.firebase.database.DataSnapshot;

import com.example.services.GenerateMeetingTimes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import com.example.DataTypes.*;

public class GroupsPage extends AppCompatActivity {

    Button createGroup, viewPending;
    static String selectedGroup;
    static DataSnapshot groupsSnap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DBConnection dbc = DBConnection.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_page);
        User currentUser = dbc.getCurrentUser();

        createGroup = (Button) findViewById(R.id.createGroup);
        viewPending = (Button) findViewById(R.id.pendingGroups);

        groupsSnap = dbc.getGroupsSnapshot();


        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),CreateGroup.class));
            }
        });

        viewPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<LocalDateTime> times = GenerateMeetingTimes.generateMeetingTime("d4bb10af-9ed3-4426-b3af-bdd019e565a9",12,6,2019, 600, 2330);
                System.out.println(times);
                startActivity(new Intent(getApplicationContext(), PendingGroups.class));
            }
        });


        LinearLayout linearLayout = findViewById(R.id.linLayout2);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        final ArrayList<String> groups = currentUser.getGroups();


        for (int i = 0; i < groups.size(); i++) {
            final Button button = new Button(this);
            button.setText(groupsSnap.child(groups.get(i)).child("name").getValue(String.class));
            button.setLayoutParams(params);
            button.setId(i);
            button.setTextSize(30);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int x = button.getId();
                    setSelectedGroup(groups.get(x));
                    startActivity(new Intent(getApplicationContext(), SingleGroupPage.class));
                }
            });

            linearLayout.addView(button);
        }

        DateTime startTime = new DateTime(TimeConverter.Convert(
                "12","5","2019","12","00"
        ));
        DateTime endTime = new DateTime(TimeConverter.Convert(
                "12","5","2019","12","30"
        ));
        SocialHourEvent event = new SocialHourEvent("testEvent",startTime,endTime,
                "5", UUID.randomUUID().toString(),new ArrayList<String>());

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            GoogleAccountCredential credential =
                    GoogleAccountCredential.usingOAuth2(
                            this,
                            Collections.singleton(CalendarScopes.CALENDAR));
            credential.setSelectedAccount(account.getAccount());

            new TestClass(credential, event).execute();
        }
    }

    public void setSelectedGroup(String id){
        selectedGroup = id;
    }


    //Change once events field is populated in db
    public static Group getSelectedGroup(){
        Group returnGroup = new Group(groupsSnap.child(selectedGroup).child("name").getValue(String.class), selectedGroup, new ArrayList<String>(),
                (ArrayList<String>)groupsSnap.child(selectedGroup).child("members").getValue());
        return returnGroup;
    }


}
