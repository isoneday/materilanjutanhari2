package com.imastudio.crudmakanan;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.imastudio.crudmakanan.adapter.ListMakananAdapter;
import com.imastudio.crudmakanan.helper.MyConstant;
import com.imastudio.crudmakanan.helper.SessionManager;
import com.imastudio.crudmakanan.model.DataKategoriItem;
import com.imastudio.crudmakanan.model.DataMakananItem;
import com.imastudio.crudmakanan.model.ResponseDataMakanan;
import com.imastudio.crudmakanan.model.ResponseKategoriMakanan;
import com.imastudio.crudmakanan.model.ResponseRegister;
import com.imastudio.crudmakanan.network.MyRetrofitClient;
import com.imastudio.crudmakanan.network.RestApi;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.imastudio.crudmakanan.helper.MyConstant.STORAGE_PERMISSION_CODE;

public class MakananActivity extends SessionManager implements SwipeRefreshLayout.OnRefreshListener, ListMakananAdapter.aksiklikitem {

    @BindView(R.id.spincarimakanan)
    Spinner spincarimakanan;
    @BindView(R.id.listmakanan)
    RecyclerView listmakanan;
    @BindView(R.id.refreshlayout)
    SwipeRefreshLayout refreshlayout;
    private Dialog dialog;
    private TextInputEditText edtnamamakanan;
    private Button btnuploadmakanan;
    private ImageView imgpreview;
    private Button btninsert;
    private Button btnreset;
    private Spinner spinnercarikategori;
    private Uri filepath;
    private Bitmap bitmap;
    private String strnamamakan;
    private List<DataKategoriItem> dataKategori;
    private String strkategori;
    private String strpath;
    private String striduser;
    private String strtime;
    private List<DataMakananItem> listdatamakanan;
    private Dialog dialog2;
    private EditText edtidmakanan;
    private Button btnupdate;
    private Button btndelete;
    private Spinner spincariupdatekategori;
    private Target mTarget;
    private String stridmakanan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makanan);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        Requestpermission();
        refreshlayout.setOnRefreshListener(this);
        getdatakategorimakanan(spincarimakanan);
      //  getDataMakanan(strkategori);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //menampilkan dialog dalam bentuk popup
                dialog = new Dialog(MakananActivity.this);
                dialog.setContentView(R.layout.tambahmakanan);
                dialog.setTitle("data makanan");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(false);
//inisialisasi
                edtnamamakanan = (TextInputEditText) dialog.findViewById(R.id.edtnamamakanan);
                btnuploadmakanan = (Button) dialog.findViewById(R.id.btnuploadmakanan);
                imgpreview = (ImageView) dialog.findViewById(R.id.imgupload);
                btninsert = (Button) dialog.findViewById(R.id.btninsert);

                btnreset = (Button) dialog.findViewById(R.id.btnreset);
                spinnercarikategori = (Spinner) dialog.findViewById(R.id.spincarikategori);
                //aksi
                btnuploadmakanan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                     showfilechooser(MyConstant.REQ_FILE_CHOOSE);
                    }
                });
                getdatakategorimakanan(spinnercarikategori);
                btninsert.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        strnamamakan = edtnamamakanan.getText().toString();
                        if (TextUtils.isEmpty(strnamamakan)) {
                            edtnamamakanan.setError("nama makanan tidak boleh kosong");
                            edtnamamakanan.requestFocus();
                            myanimation(edtnamamakanan);
                        } else if (imgpreview.getDrawable() == null) {
                            myToast("gambar harus dipilih");
                        } else {
                            insertdatamakanan(strkategori);
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
             }
        });
    }

    private void insertdatamakanan(String strkategori) {
        //mengambil path dari gmbar yang d i upload
        try {
            strpath = getPath(filepath);
            striduser = sessionManager.getIdUser();
//            MaxSizeImage(strpath);

        } catch (Exception e) {
            myToast("gambar terlalu besar \n silahkan pilih gambar yang lebih kecil");
            e.printStackTrace();
        }
        /**
         * Sets the maximum time to wait in milliseconds between two upload attempts.
         * This is useful because every time an upload fails, the wait time gets multiplied by
         * {@link UploadService#BACKOFF_MULTIPLIER} and it's not convenient that the value grows
         * indefinitely.
         */
        strtime = currentDate();
        try {
            new MultipartUploadRequest(c, MyConstant.UPLOAD_URL)
                    .addFileToUpload(strpath, "image")
                    .addParameter("vsiduser", striduser)
                    .addParameter("vsnamamakanan", strnamamakan)
                    .addParameter("vstimeinsert", strtime)
                    .addParameter("vskategori", strkategori)
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload();

            getDataMakanan(strkategori);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            myToast(e.getMessage());
        } catch (FileNotFoundException e) {
            myToast(e.getMessage());
            e.printStackTrace();
        }
    }

    private void getDataMakanan(String strkategori) {
        final ProgressDialog dialog = ProgressDialog.show(MakananActivity.this, "process getdatamakanan", "harap bersabar");
        String iduser = sessionManager.getIdUser();
        RestApi api = MyRetrofitClient.getInstaceRetrofit();
        Call<ResponseDataMakanan> ResponseRegisterCall = api.getdatamakanan(
                iduser, strkategori);
      //  myToast("iduser" + iduser);
        ResponseRegisterCall.enqueue(new Callback<ResponseDataMakanan>() {
            @Override
            public void onResponse(Call<ResponseDataMakanan> call, Response<ResponseDataMakanan> response) {
                dialog.dismiss();
                //hideProgressDialog();
                listdatamakanan = response.body().getDataMakanan();
                String[] id_makanan = new String[listdatamakanan.size()];
                String[] namamakanan = new String[listdatamakanan.size()];
                String[] fotomakanan = new String[listdatamakanan.size()];
                for (int i = 0; i < listdatamakanan.size(); i++) {
                    namamakanan[i] = listdatamakanan.get(i).getMakanan().toString();
                    fotomakanan[i] = listdatamakanan.get(i).getFotoMakanan().toString();
                    id_makanan[i] = listdatamakanan.get(i).getIdMakanan().toString();
                    striduser = id_makanan[i];
                }
                ListMakananAdapter adapter = new ListMakananAdapter(c, listdatamakanan);
                listmakanan.setAdapter(adapter);
                listmakanan.setLayoutManager(new LinearLayoutManager(MakananActivity.this));
                adapter.setOnClick(MakananActivity.this);
            }

            @Override
            public void onFailure(Call<ResponseDataMakanan> call, Throwable t) {
                dialog.dismiss();
                myToast(t.getMessage());
            }
        });
    }

    private String getPath(Uri filepath) {
        Cursor cursor = getContentResolver().query(filepath, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    private void getdatakategorimakanan(final Spinner spincariupdatekategori) {
        RestApi api = MyRetrofitClient.getInstaceRetrofit();
        Call<ResponseKategoriMakanan> kategorimakanCall = api.getkategorimakanan();
        kategorimakanCall.enqueue(new Callback<ResponseKategoriMakanan>() {
            @Override
            public void onResponse(Call<ResponseKategoriMakanan> call, Response<ResponseKategoriMakanan> response) {
                dataKategori = response.body().getDataKategori();
                String[] itemid = new String[dataKategori.size()];
                String[] itemnama = new String[dataKategori.size()];
                for (int i = 0; i < dataKategori.size(); i++) {
                    itemid[i] = dataKategori.get(i).getIdKategori();
                    itemnama[i] = dataKategori.get(i).getNamaKategori();
                }
                ArrayAdapter adapter = new ArrayAdapter(MakananActivity.this, android.R.layout.simple_spinner_dropdown_item, itemnama);
                spincariupdatekategori.setAdapter(adapter);
                spincariupdatekategori.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        strkategori = parent.getItemAtPosition(position).toString();
                        getDataMakanan(strkategori);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onFailure(Call<ResponseKategoriMakanan> call, Throwable t) {

            }
        });
    }

    private void showfilechooser(int reqFileChoose) {
        Intent intentgalery = new Intent(Intent.ACTION_PICK);
        intentgalery.setType("image/*");
        intentgalery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intentgalery, "select Pictures"), reqFileChoose);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MyConstant.REQ_FILE_CHOOSE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filepath = data.getData();
            try {

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
                imgpreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    private void Requestpermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);


    }

    @Override
    public void onRefresh() {
        refreshlayout.setRefreshing(false);
        getDataMakanan(strkategori);
    }

    @Override
    public void onItemClick(int position) {
        dialog2 = new Dialog(MakananActivity.this);
        dialog2.setTitle("Update data makanan");
        dialog2.setCanceledOnTouchOutside(false);
        dialog2.setContentView(R.layout.updatemakanan);
        dialog2.show();
        //inisialisasi
        edtnamamakanan = (TextInputEditText) dialog2.findViewById(R.id.edtnamamakanan);
        edtidmakanan = (EditText) dialog2.findViewById(R.id.edtidmakanan);
        btnuploadmakanan = (Button) dialog2.findViewById(R.id.btnuploadmakanan);
        imgpreview = (ImageView) dialog2.findViewById(R.id.imgupload);
        btnupdate = (Button) dialog2.findViewById(R.id.btnupdate);
        btndelete = (Button) dialog2.findViewById(R.id.btndelete);
        spincariupdatekategori = (Spinner) dialog2.findViewById(R.id.spincarikategori);

        mTarget = new Target() {
            @Override
            public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from){
                //Do something
//            ...

                imgpreview.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Picasso.with(c)
                .load(MyConstant.IMAGE_URL+listdatamakanan.get(position).getFotoMakanan().toString())
                .into(mTarget);
        //  imgpreview.setImageBitmap();

        getdatakategorimakanan(spincariupdatekategori);
        //isidata
        edtnamamakanan.setText(listdatamakanan.get(position).getMakanan());
        edtidmakanan.setText(listdatamakanan.get(position).getIdMakanan());
        btnuploadmakanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showfilechooser(MyConstant.REQ_FILE_CHOOSE);
            }
        });
        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stridmakanan = edtidmakanan.getText().toString();
                hapusdatamakanan();
            }
        });
        btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    strpath = getPath(filepath);
                    striduser = sessionManager.getIdUser();

                } catch (Exception e) {
//                    myToast("gambar terlalu besar \n silahkan pilih gambar yang lebih kecil");
                    e.printStackTrace();
                }

                strnamamakan = edtnamamakanan.getText().toString();
                stridmakanan = edtidmakanan.getText().toString();
                if (TextUtils.isEmpty(strnamamakan)) {
                    edtnamamakanan.setError("nama makanan tidak boleh kosong");
                    edtnamamakanan.requestFocus();
                    myanimation(edtnamamakanan);
                } else if (imgpreview.getDrawable() == null) {
                    myToast("gambar harus dipilih");
                }else if (strpath==null){
                    myToast("gambar harus diganti");

                }
                else {
                    /**
                     * Sets the maximum time to wait in milliseconds between two upload attempts.
                     * This is useful because every time an upload fails, the wait time gets multiplied by
                     * {@link UploadService#BACKOFF_MULTIPLIER} and it's not convenient that the value grows
                     * indefinitely.
                     */

                    try {
                        new MultipartUploadRequest(c, MyConstant.UPLOAD_UPDATE_URL)
                                .addFileToUpload(strpath, "image")
                                .addParameter("vsidmakanan", stridmakanan)
                                .addParameter("vsnamamakanan", strnamamakan)
                                .addParameter("vsidkategori", strkategori)
                                .setNotificationConfig(new UploadNotificationConfig())
                                .setMaxRetries(2)

                                .startUpload();


                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        myToast(e.getMessage());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        myToast(e.getMessage());
                    }

                    dialog2.dismiss();
                }
            }
        });
    }

    private void hapusdatamakanan() {
        showProgressDialog("proccess hapus data makanan");
        RestApi api = MyRetrofitClient.getInstaceRetrofit();
        Call<ResponseRegister> modelmakananCall = api.deletedata(
                stridmakanan
        );
        modelmakananCall.enqueue(new Callback<ResponseRegister>() {
            @Override
            public void onResponse(Call<ResponseRegister> call, Response<ResponseRegister> response) {
                String result = response.body().getResult();
                String msg = response.body().getMsg();
                if (result.equals("1")) {
                    myToast(msg);
                    dialog2.dismiss();
                    getDataMakanan(strkategori);
                } else {
                    myToast(msg);
                    dialog2.setCancelable(false);
                }
                hideProgressDialog();
            }

            @Override
            public void onFailure(Call<ResponseRegister> call, Throwable t) {
                myToast(t.getMessage());
            }
        });
    }
}
