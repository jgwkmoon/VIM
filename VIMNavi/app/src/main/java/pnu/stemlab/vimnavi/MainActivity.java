package pnu.stemlab.vimnavi;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import com.coordispace.listener.PositionDetailListener;
import com.coordispace.listener.StatusListener; // new
import com.coordispace.main.PermissionActivity;
import com.coordispace.main.Status; // new
import com.coordispace.main.ServiceManager;

class WaypointList {
    private String[] floorNames;
    private String[][] wayptNames;
    private String[][] stateIDs;

    WaypointList() {
        this.floorNames = null;
        this.wayptNames = null;
        this.stateIDs = null;
    }
    WaypointList(JSONObject jsonObject) {
        this.floorNames = null;
        this.wayptNames = null;
        this.stateIDs = null;
        setFromJSON(jsonObject);
    }
    public void load(FileInputStream fis) {
		try {
			byte inputByteString[];
			inputByteString = new byte[fis.available()];
            fis.read(inputByteString);
            setFromJSON(new JSONObject(new String(inputByteString)));
		} catch (IOException e) {
			e.printStackTrace();
		} catch(JSONException e) {
			e.printStackTrace();
		}
    }
    public void save(FileOutputStream fos) {
        try {
            String outputString = toJSON().toString();
            fos.write(outputString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setFromJSON(JSONObject jsonObject) {
        try {
            JSONArray jsonArrayFloorNames = jsonObject.getJSONArray("floorNames");
            this.floorNames = new String[jsonArrayFloorNames.length()];
            for (int i=0; i<jsonArrayFloorNames.length(); ++i)
                this.floorNames[i] = jsonArrayFloorNames.getString(i);
        } catch(JSONException e) {}
        try {
            JSONArray jsonArrayWayptNames = jsonObject.getJSONArray("wayptNames");
            this.wayptNames = new String[jsonArrayWayptNames.length()][];
            for (int i=0; i<jsonArrayWayptNames.length(); ++i) {
                JSONArray jsonArrayOnFloor = jsonArrayWayptNames.getJSONArray(i);
                this.wayptNames[i] = new String[jsonArrayWayptNames.length()];
                for(int j=0; j<jsonArrayOnFloor.length(); ++j) {
                    this.wayptNames[i][j] = jsonArrayOnFloor.getString(j);
                }
            }
        } catch(JSONException e) {}
        try {
            JSONArray jsonArrayStateIDs = jsonObject.getJSONArray("stateIDs");
            this.stateIDs = new String[jsonArrayStateIDs.length()][];
            for (int i=0; i<jsonArrayStateIDs.length(); ++i) {
                JSONArray jsonArrayOnFloor = jsonArrayStateIDs.getJSONArray(i);
                this.stateIDs[i] = new String[jsonArrayStateIDs.length()];
                for(int j=0; j<jsonArrayOnFloor.length(); ++j) {
                    this.stateIDs[i][j] = jsonArrayOnFloor.getString(j);
                }
            }
        } catch(JSONException e) {}
    }
    public JSONObject toJSON() {
        JSONObject jsonObj = new JSONObject();
        if(floorNames!=null) {
            JSONArray jsonArrayFloorNames = new JSONArray();
            for (CharSequence floorName: floorNames)
                jsonArrayFloorNames.put(floorName.toString());
            try { jsonObj.put("floorNames", jsonArrayFloorNames); } catch(JSONException e) {}

            JSONArray jsonArrayWayptNames = new JSONArray();
            for (CharSequence[] wayptNames: wayptNames) {
                JSONArray jsonArrayOnFoor = new JSONArray();
                for (CharSequence wayptName : wayptNames)
                    jsonArrayOnFoor.put(wayptName.toString());
                jsonArrayWayptNames.put(jsonArrayOnFoor);
            }
            try { jsonObj.put("wayptNames", jsonArrayWayptNames); } catch(JSONException e) {}

            JSONArray jsonArrayStateIDs = new JSONArray();
            for (CharSequence[] stateIDs: stateIDs) {
                JSONArray jsonArrayOnFoor = new JSONArray();
                for (CharSequence stateID : stateIDs)
                    jsonArrayOnFoor.put(stateID.toString());
                jsonArrayStateIDs.put(jsonArrayOnFoor);
            }
            try { jsonObj.put("stateIDs", jsonArrayStateIDs); } catch(JSONException e) {}
        }
        return jsonObj;
    }
    public CharSequence getFloor(int floorID) {
        return floorNames[floorID];
    }
    public CharSequence getPoint(int floorID, int stateID) {
        return wayptNames[floorID][stateID];
    }
    public CharSequence getState(int floorID, int stateID) {
        return stateIDs[floorID][stateID];
    }
    public CharSequence state2Point(CharSequence stateID) {
        for(int floorID=0; floorID<stateIDs.length; ++floorID)
            for(int pointID=0; pointID<stateIDs[floorID].length; ++pointID)
                if(stateIDs[floorID][pointID].equals(stateID))
                    return wayptNames[floorID][pointID];
        return null;
    }
    public CharSequence point2State(String wayptName) {
        for(int floorID=0; floorID<wayptNames.length; ++floorID)
            for(int pointID=0; pointID<wayptNames[floorID].length; ++pointID)
                if(wayptNames[floorID][pointID].equals(wayptName))
                    return stateIDs[floorID][pointID];
        return null;
    }
    public CharSequence[] getStartingFloors() {
        CharSequence[] selectiveFloor = new CharSequence[floorNames.length + 2];
        int i=0;
        selectiveFloor[i++] = "Back to Previous";
        selectiveFloor[i++] = "Current Floor";
        for(CharSequence floorName: floorNames)
            selectiveFloor[i++] = floorName;
        return selectiveFloor;
    }
    public CharSequence[] getStartingPoints(int floorID) {
        CharSequence[] selectivePoints = new CharSequence[wayptNames[floorID].length + 2];
        int i=0;
        selectivePoints[i++] = "Back to Previous";
        selectivePoints[i++] = "Current Point";
        for(CharSequence stateName: wayptNames[floorID])
            selectivePoints[i++] = stateName;
        return selectivePoints;
    }
    public CharSequence[] getDestinationFloors() {
        CharSequence[] selectiveFloor = new CharSequence[floorNames.length + 1];
        int i=0;
        selectiveFloor[i++] = "Back to Previous";
        for(CharSequence floorName: floorNames)
            selectiveFloor[i++] = floorName;
        return selectiveFloor;
    }
    public CharSequence[] getDestinationPoints(int floorID) {
        CharSequence[] selectivePoints = new CharSequence[wayptNames[floorID].length + 1];
        int i=0;
        selectivePoints[i++] = "Back to Previous";
        for(CharSequence stateName: wayptNames[floorID])
            selectivePoints[i++] = stateName;
        return selectivePoints;
    }
}

class RoutingIndex {
    private int selectedStartingFloorIndex;
    private int selectedStartingPointIndex;
    private int selectedDestinationFloorIndex;
    private int selectedDestinationPointIndex;

    public RoutingIndex() {
        reset();
    }
    public void reset() {
        this.selectedStartingFloorIndex = 0;
        this.selectedStartingPointIndex = 0;
        this.selectedDestinationFloorIndex = 0;
        this.selectedDestinationPointIndex = 0;
    }

    public void setStartingFloorIndex(int index) {
        selectedStartingFloorIndex = index;
    }
    public void setStartingPointIndex(int index) {
        selectedStartingPointIndex = index;
    }
    public void setDestinationFloorIndex(int index) {
        selectedDestinationFloorIndex = index;
    }
    public void setDestinationPointIndex(int index) {
        selectedDestinationPointIndex = index;
    }

    public int getStaringFloorIndex() {
        return selectedStartingFloorIndex;
    }
    public int getStartingPointIndex() {
        return selectedStartingPointIndex;
    }
    public int getDestinationFloorIndex() {
        return selectedDestinationFloorIndex;
    }
    public int getDestinationPointIndex() {
        return selectedDestinationPointIndex;
    }

    public int getStaringFloorID(int currentFloorID) {
        if(selectedStartingFloorIndex==1)
            return currentFloorID;
        return selectedStartingFloorIndex-2;
    }
    public int getStartingPointID(int currentStateID) {
        if(selectedStartingPointIndex==1)
            return currentStateID;
        return selectedStartingPointIndex-2;
    }
    public int getDestinationFloorID() {
        return selectedDestinationFloorIndex-1;
    }
    public int getDestinationPointID() {
        return selectedDestinationPointIndex-1;
    }

    public boolean isValidRoutingPoint() {
        return selectedStartingFloorIndex!=0 && selectedStartingPointIndex!=0 &&
                selectedDestinationFloorIndex!=0 && selectedDestinationPointIndex!=0;
    }
}

class CoordiPos {
    private float x;
    private float y;
    private int layer;
    private float dir;
    private long time;

    public CoordiPos() {
        this.x = Float.NaN;
        this.y = Float.NaN;
        this.layer = Integer.MIN_VALUE;
        this.dir = Float.NaN;
        this.time = Long.MIN_VALUE;
    }
    public CoordiPos(float x, float y, int layer, float heading, long time) {
        set(x, y, layer, heading, time);
    }
    public void set(float x, float y, int layer, float dir, long time) {
        this.x = x;
        this.y = y;
        this.layer = layer;
        this.dir = dir;
        this.time = time;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public int getLayer() {
        return layer;
    }
    public float getDir() {
        return dir;
    }
    public long getTime() {
        return time;
    }
    @Override
    public String toString() {
        Date date = new Date(time);
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss.S");
        String strTime = fmt.format(date);
        return String.format("%.3f\t%.3f\t%d\t%.3f\t%s\n",
            x, y, layer, dir, strTime);
    }
}

class VIMPos {
    private double x;
    private double y;
    private double z;
    private double dir;
    private long time;

    public VIMPos() {
        this.x = Double.NaN;
        this.y = Double.NaN;
        this.z = Double.NaN;
        this.dir = Double.NaN;
        this.time = Long.MIN_VALUE;
    }
    public VIMPos(double x, double y, double z, double dir, long time) {
        set(x, y, z, dir, time);
    }
    public VIMPos(CoordiPos coordiPos) {
        set(coordiPos);
    }
    public void set(double x, double y, double z, double dir, long time) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dir = dir;
        this.time = time;
    }
    public void set(CoordiPos coordiPos) {
        final double minX = 22653.426651492573;
        final double maxX = 95105.91663775852;
        final double minY = 28103.24025076769;
        final double maxY = 63401.9560;

        double X = coordiPos.getX()*1000.0 + minX;
        double Y = (35.46 - coordiPos.getY())*1000.0 + minY;
        double Z = (double)coordiPos.getLayer() * 3000.0;
        double DIR = (double)coordiPos.getDir();
        DIR = DIR < 0.0 ? -DIR : 360.0-DIR;
        set(X, Y, Z, DIR, time);
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getZ() {
        return z;
    }
    public double getDir() {
        return dir;
    }
    public long getTime() {
        return time;
    }
    @Override
    public String toString() {
        Date date = new Date(time);
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss.S");
        String strTime = fmt.format(date);
        return String.format("%.3f\t%.3f\t%.3f\t%.3f\t%s\n",
                x, y, z, dir, strTime);
    }
}

public class MainActivity extends AppCompatActivity {
    // codes for navigation menu (begin)
    private boolean inNavigation = false;
    private boolean inSuspend = false;
    private boolean isJustStarted = true;
    private boolean inDebug = false;
    private WaypointList routingStates = new WaypointList();
    private RoutingIndex routingIndex = new RoutingIndex();

    private void loadWaypoint(String fileName) {
        try {
            FileInputStream fis = openFileInput(fileName);
            routingStates.load(fis);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void saveWaypoint(String fileName) {
        try {
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            routingStates.save(fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int getCurrentFloorID() {
        return 0;
    }
    private int getCurrentPointID() {
        return 0;
    }
    private void setOutOfNaviState() {
        inNavigation = false;
        inSuspend = false;
        isJustStarted = false;
        Button naviButton = findViewById(R.id.naviButton);
        naviButton.setText("NAVIGATION");
        Button repeatButton = findViewById(R.id.repeatButton);
        repeatButton.setVisibility(View.INVISIBLE);
        Button suspendResumeButton = findViewById(R.id.suspendResumeButton);
        suspendResumeButton.setVisibility(View.INVISIBLE);
    }
    private void setInNaviState() {
        inNavigation = true;
        inSuspend = false;
        isJustStarted = true;
        Button naviButton = findViewById(R.id.naviButton);
        naviButton.setText("STOP");
        Button repeatButton = findViewById(R.id.repeatButton);
        repeatButton.setVisibility(View.VISIBLE);
        Button suspendResumeButton = findViewById(R.id.suspendResumeButton);
        suspendResumeButton.setVisibility(View.VISIBLE);
        accuInsts.clear();
    }
    public static List<String> getRoutingPath(Graph indoorNet, String startVID, String endVID) {
        List<Edge> edges = indoorNet.getEdges();
        DGraph.Edge[] dgEdges = new DGraph.Edge[edges.size()];
        for(int i=0; i<edges.size(); ++i) {
            Edge e = edges.get(i);
            String srcVID = e.src.id;
            String dstVid = e.dst.id;
            double weight = e.src.point.getDist3(e.dst.point);
            dgEdges[i] = new DGraph.Edge(srcVID, dstVid, weight);
        }
        List<String> stateIDPath = ShortestPath.getRoutingPath(dgEdges, startVID, endVID);

        Watch.putln("VIM_MSG", "* Shortest Path (VID, before removing)");
        Watch.putln("VIM_MSG", stateIDPath.toString() );

        // if not start, end, intersection or corner, remove that states.
        for(int i=stateIDPath.size()-1; i>=0; --i) {
            String vid = stateIDPath.get(i);
            Vertex vertex = indoorNet.getVertex(vid);
            boolean notRemovedCondition =
                false
                || vid.equals(startVID)
                || vid.equals(endVID)
                || vertex.type!=null && vertex.type.equals("intersection")
                || vertex.type!=null && vertex.type.equals("corner")
                || vertex.type!=null && vertex.type.equals("elevator")
                ;
            if( !notRemovedCondition )
                stateIDPath.remove(i);
        }

        Watch.putln("VIM_MSG", "* Shortest Path (VID, after removing)");
        Watch.putln("VIM_MSG", stateIDPath.toString() );
        return stateIDPath;
    }
    private List<String> stateIDPath = null;
    private String startStateVID;
    private String destStateVID;

    private int staringFloorIndex;
    private int startingPointIndex;
    private int destinationFloorIndex;
    private int destinationPointIndex;

    private void selectNaviStart() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int startingFloorID = routingIndex.getStaringFloorID(getCurrentFloorID());
        int startingPointID = routingIndex.getStartingPointID(getCurrentPointID());
        int destinationFloorID = routingIndex.getDestinationFloorID();
        int destinationPointID = routingIndex.getDestinationPointID();
        builder.setMessage(
            String.format("You chose the route from %s on %s to %s on %s.\n",
                routingStates.getPoint(startingFloorID, startingPointID),
                routingStates.getFloor(startingFloorID),
                routingStates.getPoint(destinationFloorID, destinationPointID),
                routingStates.getFloor(destinationFloorID)
            ) +
            String.format("Press the Start button when you are ready.")
        );

        staringFloorIndex = routingIndex.getStaringFloorIndex();
        startingPointIndex = routingIndex.getStartingPointIndex();
        destinationFloorIndex = routingIndex.getDestinationFloorIndex();
        destinationPointIndex = routingIndex.getDestinationPointIndex();

        if (startingPointIndex == 1) {
            VIMPoint curPt = new VIMPoint(vimPos.getX(), vimPos.getY(), vimPos.getZ());
            Vertex nearest = indoorNet.getNearestVertex(curPt);
            startStateVID = nearest.id;
        } else {
            startStateVID = routingStates.getState(startingFloorID, startingPointID).toString();
        }
        destStateVID = routingStates.getState(destinationFloorID, destinationPointID).toString();

        String msg = String.format("%s(%d,%d) -> %s(%d,%d)",
            startStateVID, staringFloorIndex, startingPointIndex,
            destStateVID, destinationFloorIndex, destinationPointIndex
        );
        stateIDPath = getRoutingPath(indoorNet, startStateVID, destStateVID);
        path = constructRoutingPath2(indoorNet, stateIDPath);

        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                setInNaviState();
                String msg =
                    String.format("* Routing Path\n")
                    + String.format("  START: %s, END: %s\n  ", startStateVID, destStateVID)
                    + String.format("  staringFloorIndex: %d", staringFloorIndex)
                    + String.format(", startingPointIndex: %d\n  ", startingPointIndex)
                    + String.format("  destinationFloorIndex: %d", destinationFloorIndex)
                    + String.format(", destinationPointIndex: %d\n  ", destinationPointIndex)
                    + stateIDPath.toString();
                routingPath.setText(msg);
                readMessage("Indoor Navigating Started......");
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void selectDestinationPoint() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Destination Point");
        int floorID = routingIndex.getDestinationFloorID();
        CharSequence[] destinationPoints = routingStates.getDestinationPoints(floorID);
        builder.setItems(destinationPoints, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
            int floorID = routingIndex.getDestinationFloorID();
            CharSequence[] destinationPoints = routingStates.getDestinationPoints(floorID);
//            Toast.makeText(getApplicationContext(), destinationPoints[item], Toast.LENGTH_SHORT).show();
            routingIndex.setDestinationPointIndex(item);
            if(item==0)
//                routingIndex.reset(); // previous --> cancel
                selectDestinationFloor(); // previous dialog box
            else
                selectNaviStart();
            }
        } );
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
        params.width = 1100;
        params.height = 2000;
        alertDialog.getWindow().setAttributes(params);
    }
    private void selectDestinationFloor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Destination Floor");
        CharSequence[] destinationFloors = routingStates.getDestinationFloors();
        builder.setItems(destinationFloors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
            CharSequence[] destinationFloors = routingStates.getDestinationFloors();
//            Toast.makeText(getApplicationContext(), destinationFloors[item], Toast.LENGTH_SHORT).show();
            routingIndex.setDestinationFloorIndex(item);
            if(item==0)
//                routingIndex.reset(); // previous --> cancel
                selectStartingPoint(); // previous dialog box
            else
                selectDestinationPoint();
            }
        } );
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
        params.width = 1100;
        params.height = 2000;
        alertDialog.getWindow().setAttributes(params);
    }
    private void selectStartingPoint() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Starting Point");
        int floorID = routingIndex.getStaringFloorID( getCurrentFloorID() );
        CharSequence[] startingPoints = routingStates.getStartingPoints(floorID);
        builder.setItems(startingPoints, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
            int floorID = routingIndex.getStaringFloorID( getCurrentFloorID() );
            CharSequence[] startingPoints = routingStates.getStartingPoints(floorID);
//            Toast.makeText(getApplicationContext(), startingPoints[item], Toast.LENGTH_SHORT).show();
            routingIndex.setStartingPointIndex(item);
            if(item==0)
//                routingIndex.reset(); // previous --> cancel
                selectStartingFloor(); // previous dialog box
            else
                selectDestinationFloor();
            }
        } );
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
        params.width = 1100;
        params.height = 2000;
        alertDialog.getWindow().setAttributes(params);
    }
    private void selectStartingFloor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Starting Floor");
        CharSequence[] startingFloors = routingStates.getStartingFloors();
        builder.setItems(startingFloors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                CharSequence[] startingFloors = routingStates.getStartingFloors();
//                Toast.makeText(getApplicationContext(), startingFloors[item], Toast.LENGTH_SHORT).show();
                routingIndex.setStartingFloorIndex(item);
                if(item==0)
                    routingIndex.reset(); // previous --> cancel
                else
                    selectStartingPoint();
            }
        } );
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
        params.width = 1100;
        params.height = 2000;
        alertDialog.getWindow().setAttributes(params);
    }
    // codes for navigation menu (end)

    /* Variable for IPS SDK : Start */
    // IPS ServiceManager Variables
    private ServiceManager mServiceManager;
    private Context mContext;
    private final int mRequestCode = 111;
    // Server URL and Map(Service number) Setting Variables
    private static final String SERVICE_URL = "http://13.125.45.3:8080/poinsAnalytics_api/";
    private static final int[] SERVICE_NO = new int[] {
            168, // UN
            182 // 부산대 자연대 연구실험동
    };

    // Coordispace CoordiPos Variables
    private int mStatusCode = 0;
    private int mMapIdx = 0;
    private CoordiPos coordiPos = new CoordiPos();
    private VIMPos vimPos = new VIMPos();

    // TextView Variable
    private TextView viewCoordiStatus;
    private TextView viewCoordiPos;
    private TextView viewVimPos;
    private TextView routingPath;
    private TextView viewVimInst;

    public static void removeInvalidAutoInst(List<VIMInst> dst, List<VIMInst> src) {
        List<VIMInst> prevInsts = new ArrayList<VIMInst>(src);

        for(int i=0; i<dst.size(); ) {
            if( dst.get(i).isInvalidAuto(prevInsts) ) {
                dst.remove(i);
            }
            else {
                prevInsts.add( dst.get(i++) );
            }
        }
    }
    public static void removeInvalidEndInst(List<VIMInst> dst, List<VIMInst> src) {
        List<VIMInst> prevInsts = new ArrayList<VIMInst>(src);

        for(int i=0; i<dst.size(); ) {
            if( dst.get(i).isInvalidEnd(prevInsts) ) {
                dst.remove(i);
            }
            else {
                prevInsts.add( dst.get(i++) );
            }
        }
    }
    public static void removeDuplication(List<VIMInst> dst, List<VIMInst> src) {
        List<VIMInst> prevInsts = new ArrayList<VIMInst>(src);

        for(int i=0, j; i<dst.size(); ) {
            if( dst.get(i).isDupInst(prevInsts) ) {
                dst.remove(i);
            }
            else {
                prevInsts.add( dst.get(i++) );
            }
        }
    }
    public static void assignID(List<VIMInst> insts, int startID) {
        for(int i=0; i<insts.size(); ++i)
            insts.get(i).id = startID++;
    }
    List<VIMInst> accuInsts = new ArrayList<VIMInst>();

    private List<VIMInst> getNaviInst(String id, double x, double y, double z, double dir, long time) {
        VIMLocation vimLoc = new VIMLocation(id, x, y, z, dir, time);
        List<VIMInst> insts = path.getNaviInst(vimLoc);
        removeInvalidAutoInst(insts, accuInsts);
        removeInvalidEndInst(insts, accuInsts);
        removeDuplication(insts, accuInsts);
        assignID(insts, accuInsts.size());
        accuInsts.addAll(insts);
        return insts;
    }
//    private List<VIMInst> getRestartInst(String id, double x, double y, double z, double dir, long time) {
//        VIMLocation vimLoc = new VIMLocation(id, x, y, z, dir, time);
//        List<VIMInst> insts = path.getRestartInst(vimLoc);
//        removeInvalidAutoInst(insts, accuInsts);
//        removeInvalidEndInst(insts, accuInsts);
//        removeDuplication(insts, accuInsts);
//        assignID(insts, accuInsts.size());
//        accuInsts.addAll(insts);
//        return insts;
//    }
    private List<VIMShortInst> getNaviInst2(String id, double x, double y, double z, double dir, long time) {
        VIMLocation vimLoc = new VIMLocation(id, x, y, z, dir, time);
        List<VIMShortInst> insts = path.getNaviInst2(vimLoc);
        return insts;
    }
    private List<VIMInst> getSafetyInst(String id, double x, double y, double z, double dir, long time) {
        VIMLocation vimLoc = new VIMLocation(id, x, y, z, dir, time);
        List<VIMInst> insts = path.getSafetyInst(vimLoc);
        return insts;
    }
    private List<VIMShortInst> getSafetyInst2(String id, double x, double y, double z, double dir, long time) {
        VIMLocation vimLoc = new VIMLocation(id, x, y, z, dir, time);
        List<VIMShortInst> insts = path.getSafetyInst2(vimLoc);
        return insts;
    }
    private List<VIMInst> getLandInst(String id, double x, double y, double z, double dir, long time) {
        VIMLocation vimLoc = new VIMLocation(id, x, y, z, dir, time);
        List<VIMInst> insts = path.getLandmarkInst(vimLoc);
        return insts;
    }
    private List<VIMShortInst> getLandInst2(String id, double x, double y, double z, double dir, long time) {
        VIMLocation vimLoc = new VIMLocation(id, x, y, z, dir, time);
        List<VIMShortInst> insts = path.getLandmarkInst2(vimLoc);
        return insts;
    }
    private List<VIMInst> getCLInst(String id, double x, double y, double z, double dir, long time) {
        VIMLocation vimLoc = new VIMLocation(id, x, y, z, dir, time);
        List<VIMInst> insts = path.getCLInst(vimLoc);
        return insts;
    }
    private List<VIMInst> getPOIInst(String id, double x, double y, double z, double dir, long time) {
        VIMLocation vimLoc = new VIMLocation(id, x, y, z, dir, time);
        List<VIMInst> insts = path.getSafetyInst(vimLoc);
        insts.addAll( path.getLandmarkInst(vimLoc) );
        return insts;
    }
    private static String getMsgFromInsts(List<VIMInst> insts) {
        StringBuffer msg = new StringBuffer();
        for(VIMInst inst: insts) {
            msg.append(inst.getShortMessage() + "\n");
        }
        return msg.toString();
    }
    private static String getMsgFromInsts2(List<VIMShortInst> insts) {
        StringBuffer msg = new StringBuffer();
        for(VIMShortInst inst: insts) {
            msg.append(inst.getShortMessage() + "\n");
        }
        return msg.toString();
    }

    private void initializeCoordiPosSystem() {
        /* Interface Code for IPS SDK : Start */
        mContext = this;
        mServiceManager = ServiceManager.getInstance(this);
        mServiceManager.setBaseServiceInfo(SERVICE_URL, SERVICE_NO);

        viewCoordiStatus = findViewById(R.id.coordiStatus);
        viewCoordiPos = findViewById(R.id.coordiPosition);
        routingPath = findViewById(R.id.routingPath);
        viewVimPos = findViewById(R.id.vimPosition);

        viewVimInst = findViewById(R.id.vimInstruction);

        mServiceManager.setDetailListener(new PositionDetailListener() {
            @Override
            public void onPosition(int mapIdx, float x, float y, int layer,
                                   float heading, float poinsSimilarity, float[] rollPitchYaw) {
                synchronized (vimPos) {
                    mMapIdx = mapIdx;
                    float dir = (float) ((double) rollPitchYaw[2] * 180.0 / Math.PI);
                    long currTime = System.currentTimeMillis();
                    coordiPos.set(x, y, layer, dir, currTime);
                    vimPos.set(coordiPos);
                }

                if(inNavigation && !inSuspend) {
                    runOnUiThread(new Runnable() {
                        @SuppressLint("DefaultLocale")
                        @Override
                        public void run() {
                            // TODO : Make Code here for User App, Using Positioning Data
                            /* TextView Code 3 for Sample App : Start */
                            synchronized (vimPos) {
                                String id = "0";
                                double x = vimPos.getX();
                                double y = vimPos.getY();
                                double z = vimPos.getZ();
                                double dir = vimPos.getDir();
                                long time = vimPos.getTime();

                                List<VIMShortInst> allInsts = new ArrayList<>();

                                List<VIMShortInst> safetyInsts = getSafetyInst2(id, x, y, z, dir, time);
                                allInsts.addAll(safetyInsts);

                                List<VIMShortInst> naviInsts = null;
                                if (isJustStarted) {
                                    path.initNavi();
                                    isJustStarted = false;
                                }
                                naviInsts = getNaviInst2(id, x, y, z, dir, time);
                                for (VIMShortInst inst : naviInsts) {
                                    if (inst.type == VIMShortInst.InstType.ARRIVAL) {
                                        inNavigation = false;
                                        inSuspend = false;
                                        isJustStarted = false;
                                        setOutOfNaviState();
                                    }
                                }
                                if (naviInsts != null) {
                                    allInsts.addAll(naviInsts);
                                }

                                List<VIMShortInst> LandInsts = getLandInst2(id, x, y, z, dir, time);
                                allInsts.addAll(LandInsts);

                                if (allInsts != null && allInsts.size() > 0) {
                                    String msg = getMsgFromInsts2(allInsts);

                                    viewVimInst.setText("* VIM-Instruction\n" + prevMsg.toString() + msg);
                                    readMessage(msg);
                                    prevMsg = msg;
                                }
                            }
                            /* TextView Code 3 for Sample App : End */
                        }
                    });
                }
                runOnUiThread(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        synchronized (vimPos) {
                            // TODO : Make Code here for User App, Using Positioning Data
                            /* TextView Code 3 for Sample App : Start */
                            String msg1 =
                                    String.format("* IPS (MID: %d)\n", mMapIdx)
                                            + String.format("  = X: %6.3f,", coordiPos.getX())
                                            + String.format("  Y: %6.3f\n", coordiPos.getY())
                                            + String.format("  = Layer ID: %d,", coordiPos.getLayer())
                                            + String.format("  DIR: %6.3f", coordiPos.getDir());
                            String msg2 =
                                    String.format("* VIM (MID: %d)\n", mMapIdx)
                                            + String.format("  = X: %6.3f,", vimPos.getX())
                                            + String.format("  Y: %6.3f\n", vimPos.getY())
                                            + String.format("  = Z: %6.3f,", vimPos.getZ())
                                            + String.format("  DIR: %6.3f", vimPos.getDir());
                            viewCoordiPos.setText(msg1);
                            viewVimPos.setText(msg2);
                            /* TextView Code 3 for Sample App : End */
                        }
                    }
                });
            }
        });

        mServiceManager.setStatusListener(new StatusListener() {
            @Override
            public void onStatus(int statusCode) {
                mStatusCode = statusCode;
                runOnUiThread(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        String IpsEngineStatus =
                            "* CoordiSys Status: " + mStatusCode +
                            " : " + Status.sdkStatus.get(mStatusCode);
                        viewCoordiStatus.setText(IpsEngineStatus);
                    }
                });
            }

        });

        Intent intent = new Intent(mContext, PermissionActivity.class);
        startActivityForResult(intent, mRequestCode);
        /* Interface Code for IPS SDK : End */

    }

//    private static final String indoorNetworkFileName = "B313.gml";

//  private static final String routingPathFileName = "B313Path1.txt";
//	private static final String vimTrajFileName = "B313-trajectory1-millimeter-whole-endmatch.json";
//	private static final String vimTrajFileName = "B313-trajectory1-millimeter-whole-extended.json";
//  private static final String vimTrajFileName = "B313-trajectory1-millimeter-edge-dist.json";
//	private static final String vimTrajFileName = "B313-trajectory1-millimeter-edge-mind.json";
//	private static final String vimTrajFileName = "B313-trajectory1-millimeter-edge-maxd.json";

//	private static final String routingPathFileName = "B313Path2.txt";
//	private static final String vimTrajFileName = "B313-trajectory2-millimeter-whole-endmatch.json";
//	private static final String vimTrajFileName = "B313-trajectory2-millimeter-whole-extended.json";
//	private static final String vimTrajFileName = "B313-trajectory2-millimeter-edge-dist.json";
//	private static final String vimTrajFileName = "B313-trajectory2-millimeter-edge-mind.json";
//	private static final String vimTrajFileName = "B313-trajectory2-millimeter-edge-maxd.json";

//	private static final String routingPathFileName = "B313Path3.txt";
//	private static final String vimTrajFileName = "B313-trajectory3-millimeter-whole-endmatch.json";
//	private static final String vimTrajFileName = "B313-trajectory3-millimeter-whole-extended.json";
//	private static final String vimTrajFileName = "B313-trajectory3-millimeter-edge-dist.json";
//	private static final String vimTrajFileName = "B313-trajectory3-millimeter-edge-mind.json";
//	private static final String vimTrajFileName = "B313-trajectory3-millimeter-edge-maxd.json";

//    private static final String routingPathFileName = "B313Path-S341-S386.txt";

    private static final String indoorNetworkFileName = "B313_small.gml";
    private static final String wayPointFileName = "waypoint.json";
//    private static final String routingPathFileName = "B313_small_Path1.txt";

    private Graph indoorNet = null;
    private VIMRoutingPath path = null;
    private String prevMsg = "No Instruction\n";
//    private List<VIMInst> prevInsts = new ArrayList<VIMInst>();

    private Graph loadingIndoorNet(String fileName) {
        Graph indoorNet = null;
        try {
            FileInputStream fis = openFileInput(fileName);
            indoorNet = new Graph(fis);
            fis.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return indoorNet;
    }
    private static List<String> loadRoutingPathVIDs(InputStream is, int fileSize) throws IOException {
        List<String> pathIDs = new ArrayList<String>();
        byte[] inputByteStr = new byte[fileSize];
        is.read(inputByteStr);
        Scanner sc = new Scanner(new String(inputByteStr));
        while(sc.hasNext()) {
            pathIDs.add( sc.next() );
        }
        sc.close();
        return pathIDs;
    }
    private List<String> loadRoutingPathVIDs(String fileName) {
        List<String> pathIDs = new ArrayList<String>();
        try {
			FileInputStream fis = openFileInput(fileName);	   // for android
            pathIDs = loadRoutingPathVIDs(fis, fis.available());
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathIDs;
    }
    private VIMRoutingPath constructRoutingPath2(Graph indoorNet, List<String> routingPathVIDs) {
        double minDist = 1500.0; // 1.0 m
        double advDist = 2550.0; // 2.55 m (0.85 m x 3 steps)
        double autoDist = 10000.0; // 10 m
        double safetyDist = 4000.0; // 4m
        double landDist = 4000.0; /// 4m
        double minDir = 10.0; // 10 degrees
        double distPerCycle = 1400.0; // 1.4 m/sec
        double distPerStep = 850; // 0.85 m/sec

        VIMRoutingPath.NaviParam naviParam = new VIMRoutingPath.NaviParam(
                minDist, advDist, autoDist,
                safetyDist, landDist,
                minDir, distPerCycle, distPerStep
        );
        VIMRoutingPath routingPath = new VIMRoutingPath(indoorNet, routingPathVIDs, naviParam);
        return routingPath;
    }
    private VIMRoutingPath constructRoutingPath(Graph indoorNet, String fileName) {
        List<String> routingPathVIDs = loadRoutingPathVIDs(fileName);
        double minDist = 1500.0; // 1.0 m
        double advDist = 2550.0; // 2.55 m (0.85 m x 3 steps)
        double autoDist = 10000.0; // 10 m
        double safetyDist = 4000.0; // 4m
        double landDist = 4000.0; /// 4m
        double minDir = 10.0; // 10 degrees
        double distPerCycle = 1400.0; // 1.4 m/sec
        double distPerStep = 850; // 0.85 m/sec

        VIMRoutingPath.NaviParam naviParam = new VIMRoutingPath.NaviParam(
                minDist, advDist, autoDist,
                safetyDist, landDist,
                minDir, distPerCycle, distPerStep
        );
        VIMRoutingPath routingPath = new VIMRoutingPath(indoorNet, routingPathVIDs, naviParam);
        return routingPath;
    }
    private void initializeVIMInfo() {
        // loading graph
        indoorNet = loadingIndoorNet(indoorNetworkFileName);
        if(indoorNet!=null)
            Watch.putln("VIM_MSG", "* Indoor Network Loading Complete");
        Watch.putln("VIM_MSG", indoorNet.toString());

        // loading routing path

//        path = constructRoutingPath(indoorNet, routingPathFileName);
        stateIDPath = getRoutingPath(indoorNet, "S341", "S267");
        path = constructRoutingPath2(indoorNet, stateIDPath);
        Watch.putln("VIM_MSG", "* Routing Path Construction Complete");
        Watch.putln("VIM_MSG", path.toString());
    }

    // TTS
    private TextToSpeech tts;
    private boolean isReadyTTS = false;
    private void initializeTTS() {
        tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(Locale.US);
                    if(result==TextToSpeech.LANG_MISSING_DATA || result==TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    } else {
                        tts.setPitch(1.0f);
                        tts.setSpeechRate(1.0f);
                        isReadyTTS = true;
                    }
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });
    }
    private void readMessage(String msg) {
        if( isReadyTTS ) {
            if(msg.length()>0) {
                tts.speak(msg, TextToSpeech.QUEUE_ADD, null);
            }
            else {
                tts.speak("No Instruction", TextToSpeech.QUEUE_ADD, null);
            }
        } else {
            Log.e("error", "TTS not ready!");
        }
    }

    private void readScenarioMessages() {
        String ments[] = {
                "Indoor Navigating started.  ",
                "Go 35 steps forward.  ",
                "Warning, firedoor is 5 steps away on your way.  ",
                "Go 32 steps forward.  ",
                "Go 25 steps forward.  ",
                "Go 20 steps forward.  ",
                "Go 12 steps forward.  ",
                "Warning, fire extinguisher is 5 steps away on your 1 o\'clock.  ",
                "Go 8 steps forward.  ",
                "Stop at 2 steps ahead.  ",
                "Turn left.  ",
                "Go 6 steps forward.  ",
                "Elevator at 3 steps ahead.  ",
                "Board the elevator.  ",
                "Press the button to 3th floor.  ",
                "Turn to the door.  ",
                "Wait until arriving the target floor.  ",
                "Go ahead.  ",
                "Go 5 steps forward.  ",
                "Stop at 2 steps ahead.  ",
                "Turn Right.  ",
                "Go 37 steps forward.  ",
                "Go 25 steps forward.  ",
                "Go 20 steps forward.  ",
                "Warning, fire extinguisher is 4 steps away on your 1 o\'clock.  ",
                "Go 12 steps forward.  ",
                "Shoe closet is 5 steps away on your 1 o\'clock.  ",
                "Warning, fire door is 4 steps away on your way.  ",
                "Warning, fire extinguisher is 4 steps away on your 10 o\'clock.  ",
                "Turn Left.  ",
                "Go 11 steps forward.  ",
                "Go 3 steps forward.  ",
                "Stop, you have arrived.  ",
        };
        for(String ment: ments) {
            readMessage(ment);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing Navi-GUI Components
        Button repeatButton = findViewById(R.id.repeatButton);
        repeatButton.setVisibility(View.INVISIBLE);
        Button suspendResumeButton = findViewById(R.id.suspendResumeButton);
        suspendResumeButton.setVisibility(View.INVISIBLE);
        loadWaypoint(wayPointFileName);
        LinearLayout debugLayer = findViewById(R.id.debugLayer);
        debugLayer.setVisibility(View.GONE);

        initializeVIMInfo(); // initializing VIM-Information
        initializeTTS(); // initializeTTS
        initializeCoordiPosSystem(); // initializing Coordi-System
    }

    public void onNaviButton(View view) {
        if(inNavigation) {
            setOutOfNaviState();
        } else {
            selectStartingFloor();
        }
        Log.i("VIM_INFO", "onNaviButton");
    }
    public void onCurrentButton(View view) {
//        readScenarioMessages(); // for making scenario

        String id = "0";
        double x = vimPos.getX();
        double y = vimPos.getY();
        double z = vimPos.getZ();
        double dir = vimPos.getDir();
        long time = vimPos.getTime();

        List<VIMInst> insts = getCLInst(id, x, y, z, dir, time);
        if (insts.size() > 0) {
            String msg = getMsgFromInsts(insts);
            viewVimInst.setText("* VIM-Instruction\n" + msg);
            readMessage(msg);
            prevMsg = msg;
        }
    }
    public void onNearbyButton(View view) {
        String id = "0";
        double x = vimPos.getX();
        double y = vimPos.getY();
        double z = vimPos.getZ();
        double dir = vimPos.getDir();
        long time = vimPos.getTime();

        List<VIMInst> insts = getPOIInst(id, x, y, z, dir, time);
        if (insts.size() > 0) {
            String msg = getMsgFromInsts(insts);
            viewVimInst.setText("* VIM-Instruction\n" + msg);
            readMessage(msg);
            prevMsg = msg;
        }
        Log.i("VIM_INFO", "onNearbyButton");
    }
    public void onRepeatButton(View view) {
        readMessage(prevMsg);
        Log.i("VIM_INFO", "onRepeatButton");
    }
    public void onSuspendResumeButton(View view) {
        if(inSuspend) {
            // should make the current state to not-suspeneded state.
            isJustStarted = true;
            inSuspend = false;
            Toast.makeText(getApplicationContext(), "Resumed", Toast.LENGTH_SHORT).show();
            Button suspendResumeButton = findViewById(R.id.suspendResumeButton);
            suspendResumeButton.setText("Suspend");
        } else {
            // should make the current state to suspeneded state.
            inSuspend = true;
            Toast.makeText(getApplicationContext(), "Suspended", Toast.LENGTH_SHORT).show();
            Button suspendResumeButton = findViewById(R.id.suspendResumeButton);
            suspendResumeButton.setText("Resume");
        }
        Log.i("VIM_INFO", "onSuspendResumeButton");
    }
    public void onDebugButton(View view) {
        if(inDebug) {
            inDebug = false;
            LinearLayout debugLayer = findViewById(R.id.debugLayer);
            debugLayer.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Out Of Debugging", Toast.LENGTH_SHORT).show();
        } else {
            inDebug = true;
            LinearLayout debugLayer = findViewById(R.id.debugLayer);
            debugLayer.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), "In Debugging", Toast.LENGTH_SHORT).show();
        }
        Log.i("VIM_INFO", "onDebugButton");
    }

    /* IPS Service Start/Spot Control Code for IPS SDK : Start */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data ) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == mRequestCode) {
            if( resultCode == RESULT_OK ) {
                mServiceManager.startService();
            }
            else {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServiceManager.stopService();
        tts.stop();
        Log.d("IPS_SDK", "\t MainActivity onDestroy :\t mServiceManager.stopService");
    }
    /* IPS Service Start/Spot Control Code for IPS SDK : End */
}
