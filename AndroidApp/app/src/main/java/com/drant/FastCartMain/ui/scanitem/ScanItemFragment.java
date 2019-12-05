package com.drant.FastCartMain.ui.scanitem;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.drant.FastCartMain.DownloadImageTask;
import com.drant.FastCartMain.FirebaseCallback;
import com.drant.FastCartMain.Item;
import com.drant.FastCartMain.R;
import com.drant.FastCartMain.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ScanItemFragment extends Fragment implements FirebaseCallback {
    AlertDialog.Builder dialogBuilder;
    AlertDialog alertDialog;
    SurfaceView surfaceView;
    private Boolean scanStatus;
    private Long scanTime;

    private CameraSource cameraSource;
    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String product_label="PRODUCT_LABEL";
    private String product_desc="PRODUCT_DESC";
    private String product_id="PRODUCT_ID";
    private String product_image="PRODUCT_IMAGE";
    private String cart_id="CART_ID";
    private static final int REQUEST_CAMERA_PERMISSION = 201;

    View view;
    ViewGroup container;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup containerGroup, Bundle savedInstanceState) {
        container = containerGroup;
        view = inflater.inflate(R.layout.activity_scan_barcode, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        scanStatus=true;
        scanTime = System.currentTimeMillis();
        surfaceView=view.findViewById(R.id.surfaceView);
        TextView welcomeMsg= view.findViewById(R.id.welcomeMsg);
        TextView instructions = view.findViewById(R.id.instructions);
        TextView txtBarcodeValue=view.findViewById(R.id.txtBarcodeValue);

        // Initialize Firebase + Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Profile Filling
        String uid = mAuth.getCurrentUser().getUid();

        //Definitions for barcode and qr scanners
        BarcodeDetector qrDetector = new BarcodeDetector.Builder(getActivity())
                .setBarcodeFormats(256)//QR_CODE)
                .build();
        qrDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0 && scanStatus && System.currentTimeMillis()>scanTime+2500) {
                    txtBarcodeValue.post(new Runnable() {
                        @Override
                        public void run() {
                            cart_id = barcodes.valueAt(0).displayValue;

                            //Throttle
                            scanStatus = false;

                            //Vibrate
                            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(200);

                            //Progress
                            progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Light_Dialog);
                            progressDialog.setIndeterminate(true);
                            progressDialog.setMessage("Finding Trolley...");
                            progressDialog.show();

                            if (cart_id.matches("[A-Za-z0-9]+")) {
                                DocumentReference docRef = db.collection("trolleys").document(cart_id);
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().exists()) {
                                                progressDialog.dismiss();
                                                Toast.makeText(getActivity(),"Trolley Added",Toast.LENGTH_SHORT).show();
                                                dbHandler.linkTrolleyAndUser(uid,cart_id);
                                                scanStatus=true;
                                                scanTime = System.currentTimeMillis() - 1000;
                                            } else {
                                                progressDialog.dismiss();
                                                Toast.makeText(getActivity(),"Trolley Not Found",Toast.LENGTH_SHORT).show();
                                                scanStatus=true;
                                                scanTime = System.currentTimeMillis() - 1000;
                                            }
                                        } else {
                                            Log.d("Firestore", "get failed with ", task.getException());
                                        }
                                    }
                                });
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(getActivity(),"QR Code Issue",Toast.LENGTH_SHORT).show();
                                scanStatus=true;
                                scanTime = System.currentTimeMillis() - 1000;
                            }
                        }
                    });
                }
            }
        });

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getActivity())
                .setBarcodeFormats(1|2|4|5|8|32|64|128|512|1024)//ONLY_BAR_CODE)
                .build();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0 && scanStatus && System.currentTimeMillis()>scanTime+2500) {
                    txtBarcodeValue.post(new Runnable() {
                        @Override
                        public void run() {
                            product_id = barcodes.valueAt(0).displayValue;
                            //Throttle
                            scanStatus = false;

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

        DocumentReference docRef = db.collection("users").document(uid);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    //Stop the current camera
                    try {
                        cameraSource.stop();
                    } catch (NullPointerException ex1) {
                        ex1.printStackTrace();
                    }

                    //If a trolley does not exist, initiate QR scanner
                    if (snapshot.getData().get("trolley") == null) {
                        Log.d("Firestore", "No Trolley Detected");
                        welcomeMsg.setText("Scanning for Trolley QR");
                        instructions.setText("Scan QR code to unlock Trolley");

                        cameraSource = new CameraSource.Builder(getActivity(), qrDetector)
                                .setRequestedPreviewSize(1920, 1080)
                                .setAutoFocusEnabled(true) //you should add this feature
                                .build();
                    }

                    //If a trolley exists, initiate barcode scanner
                    else {
                        Log.d("Firestore", "Trolley " + snapshot.getData().get("trolley"));
                        welcomeMsg.setText("Scanning for Item");
                        instructions.setText("Scan Item Barcode within square to add item");

                        cameraSource = new CameraSource.Builder(getActivity(), barcodeDetector)
                                .setRequestedPreviewSize(1920, 1080)
                                .setAutoFocusEnabled(true) //you should add this feature
                                .build();
                    }

                    Utils.delay(100, new Utils.DelayCallback() {
                        @Override
                        public void afterDelay() {
                            try {
                                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    cameraSource.start(surfaceView.getHolder());
                                } else {
                                    ActivityCompat.requestPermissions(getActivity(), new
                                            String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
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
            scanStatus = true;
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
    public void itemValidationCallback(Boolean correctItem){
        if (correctItem) {
            alertDialog.dismiss();
            Toast.makeText(getActivity(), "Added Item to Cart", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void displayItemsCallback(ArrayList<Item> items){}

    @Override
    public void onPause() {
        super.onPause();
        try {
            cameraSource.release();
        } catch (NullPointerException ex1) { ex1.printStackTrace(); }
    }

    private void showAlertDialog(int layout) {
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
        new DownloadImageTask(productImage, null).execute(product_image);

        dialogBuilder.setView(layoutView);
        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        //TODO: Make productButton cancel current addition to cart
        productButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHandler.cancelOperation(ScanItemFragment.this, true);
                alertDialog.dismiss();
                Toast.makeText(getActivity(), "Removed Item From Cart", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                scanStatus = true;
                scanTime = System.currentTimeMillis() - 1000;
            }
        });
    }


}