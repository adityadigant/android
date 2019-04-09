package com.growthfile.growthfileNew;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

public class CellularInformation{
    private Context context;

    public CellularInformation(Context context){
        this.context = context;
    }

    public String getMCC(TelephonyManager tm) {
        String operator = tm.getNetworkOperator();
        return operator.substring(0, 3);
    };


    public String getMNC(TelephonyManager tm) {
        String operator = tm.getNetworkOperator();
        return operator.substring(3);
    }

    public String getRadioType(TelephonyManager tm) {
        int networkType = tm.getNetworkType();

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "GSM";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GSM";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "WCDMA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "WCDMA";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "WCDMA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "WCDMA";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "WCDMA";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return "unknown";
        }
        throw new RuntimeException("New type of network");
    }

    private JSONObject createCellTowerObject(int mcc, int mnc, int cid, int lac, int signalStrength) throws JSONException {

        JSONObject information = new JSONObject();


        information.put("signalStrength", signalStrength);
        information.put("cellId", cid);
        information.put("locationAreaCode", lac);
        information.put("mobileCountryCode", mcc);
        information.put("mobileNetworkCode", mnc);

        return information;
    }


    private JSONArray getCelltowerInfo(int networkMcc, List<CellInfo> cellInfoList) throws JSONException {

        int mcc;
        int mnc;
        int lac;
        int signalStrength;
        int cid;

        JSONArray array = new JSONArray();

        for (final CellInfo info : cellInfoList) {

            if (info instanceof CellInfoGsm) {
                final CellSignalStrengthGsm signalStrengthGsm = ((CellInfoGsm) info).getCellSignalStrength();
                final CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
                //signal strength

                cid = identityGsm.getCid();
                if (cid >= 0) {


                    lac = identityGsm.getLac();
                    signalStrength = signalStrengthGsm.getDbm();

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        System.out.print(identityGsm.getMcc());

                        mcc = identityGsm.getMcc();
                        mnc = identityGsm.getMnc();
                    } else {
                        mcc = Integer.parseInt(identityGsm.getMccString());
                        mnc = Integer.parseInt(identityGsm.getMncString());
                    }
                    array.put(createCellTowerObject(mcc, mnc, cid, lac, signalStrength));


                }
            }


            if (info instanceof CellInfoWcdma) {
                final CellSignalStrengthWcdma signalStrengthWcdma = ((CellInfoWcdma) info).getCellSignalStrength();
                final CellIdentityWcdma identityWcdma = ((CellInfoWcdma) info).getCellIdentity();

                cid = identityWcdma.getCid();
                int length = String.valueOf(cid).length();

                if (cid >= 0 && length==8) {

                    lac = identityWcdma.getLac();
                    signalStrength = signalStrengthWcdma.getDbm();

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        System.out.print(identityWcdma.getMcc());
                        mcc = identityWcdma.getMcc();
                        mnc = identityWcdma.getMnc();
                    } else {
                        mcc = Integer.parseInt(identityWcdma.getMccString());
                        mnc = Integer.parseInt(identityWcdma.getMncString());
                    }
                    array.put(createCellTowerObject(mcc, mnc, cid, lac, signalStrength));
                }

            }
            if (info instanceof CellInfoLte) {
                final CellSignalStrengthLte signalStrengthLte = ((CellInfoLte) info).getCellSignalStrength();
                final CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();

                cid = identityLte.getCi();
                if (cid >= 0) {
                    lac = identityLte.getTac();
                    signalStrength = signalStrengthLte.getDbm();

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        System.out.print(identityLte.getMcc());
                        mcc = identityLte.getMcc();
                        mnc = identityLte.getMnc();
                    } else {
                        mcc = Integer.parseInt(identityLte.getMccString());
                        mnc = Integer.parseInt(identityLte.getMncString());
                    }
                    array.put(createCellTowerObject(mcc, mnc, cid, lac, signalStrength));
                }
            }

            if (info instanceof CellInfoCdma) {
                final CellSignalStrengthCdma signalStrengthCdma = ((CellInfoCdma) info).getCellSignalStrength();
                final CellIdentityCdma identityCdma = ((CellInfoCdma) info).getCellIdentity();

                cid = identityCdma.getBasestationId();
                if (cid >= 0) {
                    lac = identityCdma.getNetworkId();
                    signalStrength = signalStrengthCdma.getDbm();
                    mnc = identityCdma.getSystemId();
                    mcc = networkMcc;
                    array.put(createCellTowerObject(mcc, mnc, cid, lac, signalStrength));
                }
            }
        }
        return array;
    }



    private JSONArray getNearbyWifiAccessPoints(List<ScanResult> wifiList) throws JSONException {

        JSONArray array = new JSONArray();
        for (int i = 0; i < wifiList.size(); i++) {
            JSONObject aps = new JSONObject();
            aps.put("macAddress", wifiList.get(i).BSSID);
            aps.put("signalStrength", wifiList.get(i).level);
            array.put(aps);
        }
        return array;

    }


    public String fullCellularInformation() throws JSONException {
        MainActivity mainActivity;
        mainActivity = new MainActivity();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED ) {
            return "";
        }

        List<CellInfo> cellInfoList = null;


            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            JSONObject json = new JSONObject();
            int mcc;
            int mnc;
            String carrier;

            if (!tm.getNetworkOperator().isEmpty()) {
                mcc = Integer.parseInt(getMCC(tm));
                mnc = Integer.parseInt(getMNC(tm));

                json.put("homeMobileCountryCode", mcc);
                json.put("homeMobileNetworkCode", mnc);
                cellInfoList = tm.getAllCellInfo();
                
                if (cellInfoList != null) {
                    try {
                        json.put("cellTowers", getCelltowerInfo(mcc, cellInfoList));
                    }catch (JSONException e){
                        mainActivity.androidException(e);
                    }

                }
            }

            //set radio type
            String radioType = getRadioType(tm);

            if (!radioType.equals("unknown")) {
                json.put("radioType", getRadioType(tm));
            }

            //set carrier
            carrier = tm.getNetworkOperatorName();

            if (carrier != null && !carrier.isEmpty()) {
                json.put("carrier", carrier);
            }

            // set wifi access points
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> wifiList = wifiManager.getScanResults();

            if (!wifiList.isEmpty()) {
                try {
                    json.put("wifiAccessPoints", getNearbyWifiAccessPoints(wifiList));
                }catch (JSONException e){
                    mainActivity.androidException(e);
                }

            }
            if(wifiList.isEmpty() && cellInfoList == null ) {
                json.put("considerIp", "true");
            }

            else {
                json.put("considerIp", "false");
            }

            return json.toString(4);

    }

}
