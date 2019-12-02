package com.drant.FastCartMain.ui.scanitem;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.drant.FastCartMain.DownloadImageTask;
import com.drant.FastCartMain.FirebaseCallback;
import com.drant.FastCartMain.Item;
import com.drant.FastCartMain.LoginActivity;
import com.drant.FastCartMain.R;
import com.drant.FastCartMain.ScannedBarcodeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ScanItemFragment extends Fragment implements FirebaseCallback {
    AlertDialog.Builder dialogBuilder;
    AlertDialog alertDialog;
    SurfaceView surfaceView;
    private Long scanTime;
    private CameraSource cameraSource;
    ProgressDialog progressDialog;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String product_label="PRODUCT_LABEL";
    private String product_desc="PRODUCT_DESC";
    private String product_id="PRODUCT_ID";
    private String product_image="PRODUCT_IMAGE";
    private static final int REQUEST_CAMERA_PERMISSION = 201;

    @Override
    public void itemValidationCallback(Boolean correctItem){}

    View view;
    ViewGroup container;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup containerGroup, Bundle savedInstanceState) {
        container = containerGroup;
        view = inflater.inflate(R.layout.activity_scan_barcode, container, false);

        return view;
    }

    public void showAlertDialog(int layout) {
        //Builds and inflates the dialog into view
        dialogBuilder = new AlertDialog.Builder(getActivity());
        View layoutView = getLayoutInflater().inflate(layout, null);

        //Binds
        ImageView productImage = layoutView.findViewById(R.id.productImage);
        TextView productLabel = layoutView.findViewById(R.id.productLabel);
        TextView productDesc = layoutView.findViewById(R.id.productDesc);
        Button productButton = layoutView.findViewById(R.id.productButton);

        //Set data
        productLabel.setText(product_label);
        productDesc.setText(product_desc);
        new DownloadImageTask(productImage).execute(product_image);

        dialogBuilder.setView(layoutView);
        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        scanTime = System.currentTimeMillis();
        alertDialog.show();

        //TODO: Make productButton cancel current addition to cart
        productButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                Toast.makeText(getActivity(), "Removed Item From Cart", Toast.LENGTH_SHORT).show();
            }
        });

        //Runnable to dismiss alert dialog
        final Runnable closeDialog = new Runnable() {
            @Override
            public void run() {
                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            }
        };

        //Handler to execute ^runnable after delay, closes further thread callbacks
        final Handler handler = new Handler();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(closeDialog);
                scanTime = System.currentTimeMillis()-2500;
            }
        });
        handler.postDelayed(closeDialog, 2000);
    }


    //TODO Shift all auth to navactivity
    @Override
    public void onPause() {
        super.onPause();
//        if (authListener != null) {
//            mAuth.removeAuthStateListener(authListener);
//        }
        cameraSource.release();
    }

    @Override
    public void onResume() {
        super.onResume();
        SurfaceView surfaceView=view.findViewById(R.id.surfaceView);
        TextView welcomeMsg= view.findViewById(R.id.welcomeMsg);
        TextView txtBarcodeValue=view.findViewById(R.id.txtBarcodeValue);
        scanTime = System.currentTimeMillis();

        // Initialize Firebase + Auth Listeners
        mAuth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
            }
        };

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        //Profile Filling
        welcomeMsg.setText("Welcome " + mAuth.getCurrentUser().getDisplayName());

        BarcodeDetector barcodeDetector =
                new BarcodeDetector.Builder(container.getContext())
                        .setBarcodeFormats(1|2|4|5|8|32|64|128|512|1024)//QR_CODE)
                        .build();
        cameraSource = new CameraSource
                .Builder(container.getContext(), barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//                cameraSource.stop();
//            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (cameraSource != null) {
                    cameraSource.release();
                    cameraSource = null;
                }
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    if (!product_desc.equals(barcodes.valueAt(0).displayValue)) {
                        txtBarcodeValue.post(new Runnable() {
                            @Override
                            public void run() {
                                //TODO: Need to lookup firebase product data and change data prior to inflating dialog view
                                product_desc = barcodes.valueAt(0).displayValue;
                            }
                        });
                    }
                }
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0 && System.currentTimeMillis()>scanTime+2500) {
                    txtBarcodeValue.post(new Runnable() {
                        @Override
                        public void run() {
                            product_id = barcodes.valueAt(0).displayValue;
                            //Throttle
                            scanTime = System.currentTimeMillis();

                            //Vibrate
                            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(200);

                            //Progress
                            progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Light_Dialog);
                            progressDialog.setIndeterminate(true);
                            progressDialog.setMessage("Finding Product...");
                            progressDialog.show();


                            //Get Firestore Data
                            dbHandler.addItemToCart(ScanItemFragment.this, product_id);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onItemCallback(Item item) {
        progressDialog.dismiss();
        if (item == null) {
            Toast.makeText(getContext(), "Product not registered in database", Toast.LENGTH_SHORT).show();
            scanTime = System.currentTimeMillis() - 1000;
        } else {
            product_label = item.getName();

            DecimalFormat df2 = new DecimalFormat("#.00");
            product_desc = "$" + df2.format(item.getPrice());
            product_image = item.getImageRef();

            // TODO: alertDialog to only disappear when item has been validated
            //Build and view
            showAlertDialog(R.layout.product_dialog);
        }
    }

    @Override
    public void displayItemsCallback(ArrayList<Item> items){}
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mAuth.signOut();
//    }

}