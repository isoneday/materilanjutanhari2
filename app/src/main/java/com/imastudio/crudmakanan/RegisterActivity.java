package com.imastudio.crudmakanan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.imastudio.crudmakanan.helper.MyFunction;
import com.imastudio.crudmakanan.model.ResponseRegister;
import com.imastudio.crudmakanan.network.MyRetrofitClient;
import com.imastudio.crudmakanan.network.RestApi;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends MyFunction {

    @BindView(R.id.edtnama)
    EditText edtnama;
    @BindView(R.id.edtalamat)
    EditText edtalamat;
    @BindView(R.id.edtnotelp)
    EditText edtnotelp;
    @BindView(R.id.spinjenkel)
    Spinner spinjenkel;
    @BindView(R.id.edtusername)
    EditText edtusername;
    @BindView(R.id.edtpassword)
    TextInputEditText edtpassword;
    @BindView(R.id.edtpasswordconfirm)
    TextInputEditText edtpasswordconfirm;
    @BindView(R.id.regAdmin)
    RadioButton regAdmin;
    @BindView(R.id.regUserbiasa)
    RadioButton regUserbiasa;
    @BindView(R.id.btnregister)
    Button btnregister;
    String[] jenkel = {"laki-laki", "perempuan"};
    private String strlevel;
    private String strjenkel;
    private String strnama;
    private String stralamat;
    private String strnohp;
    private String strusername;
    private String strpassword;
    private String strconpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        if (regAdmin.isChecked()) {
            strlevel = "admin";
        } else {
            strlevel = "user biasa";
        }
        setjeniskelamin();
    }

    private void setjeniskelamin() {
        //mengisi spinner jenis kelamin
        ArrayAdapter adapter = new ArrayAdapter(c, android.R.layout.simple_spinner_item, jenkel);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinjenkel.setAdapter(adapter);
        spinjenkel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                strjenkel = jenkel[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @OnClick({R.id.regAdmin, R.id.regUserbiasa, R.id.btnregister})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.regAdmin:
                strlevel = "admin";
                break;
            case R.id.regUserbiasa:
                strlevel = "user biasa";
                break;
            case R.id.btnregister:
                strnama = edtnama.getText().toString();
                stralamat = edtalamat.getText().toString();
                strnohp = edtnotelp.getText().toString();
                strusername = edtusername.getText().toString();
                strpassword = edtpassword.getText().toString();
                strconpassword = edtpasswordconfirm.getText().toString();
                if (TextUtils.isEmpty(strnama)) {
                    edtnama.setError("nama tidak boleh kosong");
                    edtnama.requestFocus();
                    myanimation(edtnama);
                } else if (TextUtils.isEmpty(stralamat)) {
                    edtalamat.requestFocus();
                    edtalamat.setError("alamat tidak boleh kosong");
                    myanimation(edtalamat);
                } else if (TextUtils.isEmpty(strnohp)) {
                    edtnotelp.requestFocus();
                    myanimation(edtnotelp);
                    edtnotelp.setError("no hp tidak boleh kosong");
                } else if (TextUtils.isEmpty(strusername)) {
                    edtusername.requestFocus();
                    myanimation(edtusername);
                    edtusername.setError("username tidak boleh kosong");
                } else if (TextUtils.isEmpty(strpassword)) {
                    edtpassword.requestFocus();
                    myanimation(edtpassword);
                    edtpassword.setError("password tidak boleh kosong");
                } else if (strpassword.length() < 6) {
                    myanimation(edtpassword);
                    edtpassword.setError("password minimal 6 karakter");
                } else if (TextUtils.isEmpty(strconpassword)) {
                    edtpasswordconfirm.requestFocus();
                    myanimation(edtpasswordconfirm);
                    edtpasswordconfirm.setError("password confirm tidak boleh kosong");
                } else if (!strpassword.equals(strconpassword)) {
                    edtpasswordconfirm.requestFocus();
                    myanimation(edtpasswordconfirm);
                    edtpasswordconfirm.setError("password tidak sama");
                } else {
                    registeruser();
                }
                break;
        }
    }

    private void registeruser() {
        final ProgressDialog dialog = ProgressDialog.show(c,"proses register user","please wait . . .");
        RestApi api = MyRetrofitClient.getInstaceRetrofit();
        Call<ResponseRegister> registerCall = api.registerUser(
                strnama,
                stralamat,
                strnohp,
                strjenkel,
                strusername,
                strpassword,
                strlevel
        );
        registerCall.enqueue(new Callback<ResponseRegister>() {
            @Override
            public void onResponse(Call<ResponseRegister> call, Response<ResponseRegister> response) {
                if (response.isSuccessful()) {
                  dialog.dismiss();
                    String result = response.body().getResult();
                    String msg = response.body().getMsg();
                    if (result.equals("1")) {
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseRegister> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "masalah koneksi", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            }
        });
    }
}
