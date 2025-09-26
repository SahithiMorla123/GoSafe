package com.example.gosafe; // Or your package name

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactActivity extends AppCompatActivity {

    private EditText etPhoneNumber;
    private Button btnAddContact, btnSaveSelection;
    private RecyclerView rvContacts;

    private ArrayList<String> contactList;
    private ContactAdapter adapter;

    // SharedPreferences is used to save the contacts persistently
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "EmergencyContactsPrefs";
    private static final String CONTACTS_KEY = "contacts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        // Initialize UI components
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnSaveSelection = findViewById(R.id.btnSaveSelection);
        rvContacts = findViewById(R.id.rvContacts);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load saved contacts
        loadContacts();

        // Setup RecyclerView
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(contactList, this::saveAndRemoveContact);
        rvContacts.setAdapter(adapter);

        // Add Contact button logic
        btnAddContact.setOnClickListener(v -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            if (!phoneNumber.isEmpty()) {
                if (!contactList.contains(phoneNumber)) {
                    contactList.add(phoneNumber);
                    saveContacts();
                    adapter.notifyItemInserted(contactList.size() - 1);
                    etPhoneNumber.setText(""); // Clear the input field
                } else {
                    Toast.makeText(this, "Contact already exists.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter a phone number.", Toast.LENGTH_SHORT).show();
            }
        });

        // Save and Return button logic
        btnSaveSelection.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra("selected_numbers", contactList);
            setResult(RESULT_OK, resultIntent);
            finish(); // Close this activity and go back to MainActivity
        });
    }

    private void loadContacts() {
        Set<String> contacts = sharedPreferences.getStringSet(CONTACTS_KEY, new HashSet<>());
        contactList = new ArrayList<>(contacts);
    }

    private void saveContacts() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> contactSet = new HashSet<>(contactList);
        editor.putStringSet(CONTACTS_KEY, contactSet);
        editor.apply();
    }

    private void saveAndRemoveContact(int position) {
        contactList.remove(position);
        saveContacts(); // Save the updated list
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, contactList.size());
        Toast.makeText(this, "Contact removed.", Toast.LENGTH_SHORT).show();
    }
}

// --- RecyclerView Adapter ---
// This class manages the list data and creates the views for each item in the list.
class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private final List<String> contactList;
    private final OnContactDeleteListener deleteListener;

    public interface OnContactDeleteListener {
        void onDelete(int position);
    }

    ContactAdapter(List<String> contactList, OnContactDeleteListener deleteListener) {
        this.contactList = contactList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        String contact = contactList.get(position);
        holder.tvContactNumber.setText(contact);
        holder.btnDeleteContact.setOnClickListener(v -> deleteListener.onDelete(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    // This class holds the views for a single list item.
    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactNumber;
        Button btnDeleteContact;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactNumber = itemView.findViewById(R.id.tvContactNumber);
            btnDeleteContact = itemView.findViewById(R.id.btnDeleteContact);
        }
    }
}