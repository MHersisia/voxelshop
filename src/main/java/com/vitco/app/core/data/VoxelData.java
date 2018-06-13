package com.vitco.app.core.data;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.container.VoxelLayer;
import com.vitco.app.core.data.history.HistoryChangeListener;
import com.vitco.app.core.data.history.HistoryManager;
import com.vitco.app.core.data.history.VoxelActionIntent;
import com.vitco.app.low.CubeIndexer;
import com.vitco.app.settings.VitcoSettings;
import com.vitco.app.util.graphic.GraphicTools;
import com.vitco.app.util.misc.ArrayUtil;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Defines the voxel data interaction (layer, undo, etc)
 */
public abstract class VoxelData extends AnimationHighlight implements VoxelDataInterface {

    // constructor
    // contains the (history) listener event declaration for voxel and texture
    protected VoxelData() {
        super();
        // notify when the data changes
        historyManagerV.addChangeListener(new HistoryChangeListener<VoxelActionIntent>() {
            @Override
            public final void onChange(VoxelActionIntent action) {
                int[][] effectedVoxels = null;
                boolean effectsTexture = false;
                if (action != null) {
                    effectedVoxels = action.effected();
                    effectsTexture = action.effectsTexture();
                }
                // calculate the effected voxels
                invalidateV(effectedVoxels);
                // notify if texture have been changed
                if (effectsTexture) {
                    notifier.onTextureDataChanged();
                }
            }

            @Override
            public void onFrozenIntent(VoxelActionIntent actionIntent) {
                notifier.onFrozenAction();
            }

            @Override
            public void onFrozenApply() {
                notifier.onFrozenRedo();
            }

            @Override
            public void onFrozenUnapply() {
                notifier.onFrozenUndo();
            }
        });
    }

    // holds the voxel positions that are currently selected (so we only notify
    // position where the selection state has actually changed!)
    private final TIntHashSet currentSelectedVoxel = new TIntHashSet();

    // invalidate cache
    protected final void invalidateV(int[][] effected) {
        if (effected != null) {
            // notification of changed visible voxels
            for (TIntObjectHashMap<int[]> map : changedVisibleVoxel.values()) {
                if (map != null) {
                    for (int[] invalid : effected) {
                        Integer key = CubeIndexer.getId(invalid[0], invalid[1], invalid[2]);
                        if (!map.containsKey(key)) {
                            map.put(key, invalid);
                        }
                    }
                }
            }

            // notification of changed selected voxels
            TIntObjectHashMap<int[]> invalidSelected = new TIntObjectHashMap<int[]>();
            for (int[] invalid : effected) {
                Voxel voxel = searchVoxel(invalid, false);
                if (voxel == null) {
                    int key = CubeIndexer.getId(invalid[0], invalid[1], invalid[2]);
                    if (currentSelectedVoxel.remove(key)) {
                        invalidSelected.put(key, invalid);
                    }
                } else {
                    int key = voxel.posId;
                    if (voxel.isSelected()) {
                        if (currentSelectedVoxel.add(key)) {
                            invalidSelected.put(key, invalid);
                        }
                    } else {
                        if (currentSelectedVoxel.remove(key)) {
                            invalidSelected.put(key, invalid);
                        }
                    }
                }
            }
            for (TIntObjectHashMap<int[]> map : changedSelectedVoxel.values()) {
                if (map != null) {
                    map.putAll(invalidSelected);
                }
            }

            // notification of changed visible voxel for this plane
            for (TIntIterator it = changedVisibleVoxelPlane.keySet().iterator(); it.hasNext();) {
                int side = it.next();
                int missingSide = 1;
                switch (side) {
                    case 0: missingSide = 2; break;
                    case 2: missingSide = 0; break;
                    default: break;
                }
                for (String requestId : changedVisibleVoxelPlane.get(side).keySet()) {
                    for (int[] invalid : effected) {
                        // make sure this is set
                    	HashMap<String, TIntObjectHashMap<TIntObjectHashMap<int[]>>> map = changedVisibleVoxelPlane.get(side);
                        TIntObjectHashMap<TIntObjectHashMap<int[]>> objectMap = map.get(requestId);
                        
                        if (!objectMap.containsKey(invalid[missingSide])) {
                        	objectMap.put(invalid[missingSide], new TIntObjectHashMap<int[]>());
                        }
                        // fill the details
                        int key = CubeIndexer.getId(invalid[0], invalid[1], invalid[2]);
                        
                        TIntObjectHashMap<int[]> invalidVoxelObject = objectMap.get(invalid[missingSide]);
                        
                        if (!invalidVoxelObject.containsKey(key)) {
                            invalidVoxelObject.put(key, invalid);
                        }
                    }
                }
            }
        } else {
            currentSelectedVoxel.clear();
            changedSelectedVoxel.clear();
            changedVisibleVoxel.clear();
            changedVisibleVoxelPlane.clear();
        }
        layerBufferValid = false;
        layerNameBufferValid = false;
        layerVoxelBufferValid = false;
        layerVoxelXYBufferValid = false;
        layerVoxelXZBufferValid = false;
        layerVoxelYZBufferValid = false;
        selectedVoxelBufferValid = false;
        visibleLayerVoxelInternalBufferValid = false;
        notifier.onVoxelDataChanged();
    }

    // holds the historyV data
    protected final HistoryManager<VoxelActionIntent> historyManagerV = new HistoryManager<VoxelActionIntent>();

    // buffer for the selected voxels
    private Voxel[] selectedVoxelBuffer = new Voxel[0];
    private boolean selectedVoxelBufferValid = false;

    // ===========================================

    // ###################### PRIVATE HELPER CLASSES

    

    

    // layer events

    

    // mass events

    // ##################### PRIVATE HELPER FUNCTIONS
    // returns a free voxel id
    private int lastVoxel = -1;
    int getFreeVoxelId() {
        do {
            lastVoxel++;
        } while (dataContainer.voxels.containsKey(lastVoxel));
        return lastVoxel;
    }

    // returns a free layer id
    private int lastLayer = -1;
    int getFreeLayerId() {
        do {
            lastLayer++;
        } while (dataContainer.layers.containsKey(lastLayer));
        return lastLayer;
    }

    // returns a free texture id
    private int lastTexture = -1;
    int getFreeTextureId() {
        do {
            lastTexture++;
        } while (dataContainer.textures.containsKey(lastTexture));
        return lastTexture;
    }

    // =========================
    // === interface methods ===
    // =========================

    @Override
    public final int addVoxelDirect(Color color, int[] pos) {
        synchronized (VitcoSettings.SYNC) {
            int result = -1;
            VoxelLayer layer = dataContainer.layers.get(dataContainer.selectedLayer);
            if (layer != null && layer.voxelPositionFree(pos)) {
                result = getFreeVoxelId();
                Voxel voxel = new Voxel(result, pos, color, false, null, dataContainer.selectedLayer);
                dataContainer.voxels.put(voxel.id, voxel);
                dataContainer.layers.get(voxel.getLayerId()).addVoxel(voxel);
            }
            return result;
        }
    }

    @Override
    public final int addVoxel(Color color, int[] textureId, int[] pos) {
        synchronized (VitcoSettings.SYNC) {
            int result = -1;
            VoxelLayer layer = dataContainer.layers.get(dataContainer.selectedLayer);
            if (layer != null && layer.getSize() < VitcoSettings.MAX_VOXEL_COUNT_PER_LAYER && layer.voxelPositionFree(pos)) {
                result = getFreeVoxelId();
                historyManagerV.applyIntent(new AddVoxelIntent(this, result, pos, color, false, textureId, dataContainer.selectedLayer, false));
            }
            return result;
        }
    }

    @Override
    public final boolean massAddVoxel(Voxel[] voxels) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            VoxelLayer layer = dataContainer.layers.get(dataContainer.selectedLayer);
            if (layer != null) {
                ArrayList<Voxel> validVoxel = new ArrayList<Voxel>();
                HashSet<String> voxelPos = new HashSet<String>();
                for (Voxel voxel : voxels) {
                    String posHash = voxel.getPosAsString();
                    if (layer.voxelPositionFree(voxel)
                            && !voxelPos.contains(posHash)) {
                        validVoxel.add(voxel);
                        voxelPos.add(posHash);
                    }
                }
                if (validVoxel.size() > 0 && layer.getSize() + validVoxel.size() <= VitcoSettings.MAX_VOXEL_COUNT_PER_LAYER) {
                    Voxel[] valid = new Voxel[validVoxel.size()];
                    validVoxel.toArray(valid);
                    historyManagerV.applyIntent(new MassAddVoxelIntent(this, valid, layer.id, false));
                    result = true;
                }
            }
            return result;
        }
    }

    @Override
    public final boolean removeVoxel(int voxelId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.voxels.containsKey(voxelId)) {
                historyManagerV.applyIntent(new RemoveVoxelIntent(this, voxelId, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean massRemoveVoxel(Integer[] voxelIds) {
        synchronized (VitcoSettings.SYNC) {
            ArrayList<Integer> validVoxel = new ArrayList<Integer>();
            for (int voxelId : voxelIds) {
                if (dataContainer.voxels.containsKey(voxelId)) {
                    validVoxel.add(voxelId);
                }
            }
            if (validVoxel.size() > 0) {
                Integer[] valid = new Integer[validVoxel.size()];
                validVoxel.toArray(valid);
                historyManagerV.applyIntent(new MassRemoveVoxelIntent(this, valid, false));
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public final boolean moveVoxel(int voxelId, int[] newPos) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            Voxel voxel = dataContainer.voxels.get(voxelId);
            if (voxel != null) {
                historyManagerV.applyIntent(new MoveVoxelIntent(this, voxel.id, newPos, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean massMoveVoxel(Voxel[] voxel, int[] shift) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (voxel.length > 0 && (shift[0] != 0 || shift[1] != 0 || shift[2] != 0)) {
                historyManagerV.applyIntent(new MassMoveVoxelIntent(this, voxel, shift.clone(), false));
                result = true;
            }
            return result;
        }
    }

    // rotate voxel around their center (but not the voxel "texture" itself)
    @Override
    public final boolean rotateVoxelCenter(Voxel[] voxel, int axe, float degree) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (voxel.length > 0 && degree/360 != 0 && axe <= 2 && axe >= 0) {
                historyManagerV.applyIntent(new RotateVoxelCenterIntent(this, voxel, axe, degree, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean mirrorVoxel(Voxel[] voxel, int axe) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (voxel.length > 0 && axe <= 2 && axe >= 0) {
                historyManagerV.applyIntent(new MirrorVoxelIntent(this, voxel, axe, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final Voxel getVoxel(int voxelId) {
        synchronized (VitcoSettings.SYNC) {
            Voxel result = null;
            if (dataContainer.voxels.containsKey(voxelId)) {
                result = dataContainer.voxels.get(voxelId);
            }
            return result;
        }
    }

    @Override
    public final boolean setColor(int voxelId, Color color) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.voxels.containsKey(voxelId) &&
                    (!dataContainer.voxels.get(voxelId).getColor().equals(color) ||
                            dataContainer.voxels.get(voxelId).getTexture() != null)) {
                historyManagerV.applyIntent(new ColorVoxelIntent(this, voxelId, color, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean massSetColor(Integer[] voxelIds, Color color) {
        synchronized (VitcoSettings.SYNC) {
            ArrayList<Integer> validVoxel = new ArrayList<Integer>();
            for (int voxelId : voxelIds) {
                Voxel voxel = dataContainer.voxels.get(voxelId);
                if (voxel != null && !voxel.getColor().equals(color)) {
                    validVoxel.add(voxelId);
                }
            }
            if (validVoxel.size() > 0) {
                Integer[] valid = new Integer[validVoxel.size()];
                validVoxel.toArray(valid);
                historyManagerV.applyIntent(new MassColorVoxelIntent(this, valid, color, false));
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public final boolean massShiftColor(Integer[] voxelIds, float[] hsbOffset) {
        synchronized (VitcoSettings.SYNC) {
            ArrayList<Integer> validVoxel = new ArrayList<Integer>();
            for (int voxelId : voxelIds) {
                Voxel voxel = dataContainer.voxels.get(voxelId);
                if (voxel != null) {
                    validVoxel.add(voxelId);
                }
            }
            if (validVoxel.size() > 0) {
                Integer[] valid = new Integer[validVoxel.size()];
                validVoxel.toArray(valid);
                historyManagerV.applyIntent(new MassColorShiftVoxelIntent(this, valid, hsbOffset, false));
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public final Color getColor(int voxelId) {
        synchronized (VitcoSettings.SYNC) {
            Color result = null;
            if (dataContainer.voxels.containsKey(voxelId)) {
                result = dataContainer.voxels.get(voxelId).getColor();
            }
            return result;
        }
    }

    @Override
    public final boolean setAlpha(int voxelId, int alpha) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.voxels.containsKey(voxelId) && dataContainer.voxels.get(voxelId).getAlpha() != alpha) {
                historyManagerV.applyIntent(new AlphaVoxelIntent(this, voxelId, alpha, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final int getAlpha(int voxelId) {
        synchronized (VitcoSettings.SYNC) {
            int result = -1;
            if (dataContainer.voxels.containsKey(voxelId)) {
                result = dataContainer.voxels.get(voxelId).getAlpha();
            }
            return result;
        }
    }

    @Override
    public final int getLayer(int voxelId) {
        synchronized (VitcoSettings.SYNC) {
            int result = -1;
            if (dataContainer.voxels.containsKey(voxelId)) {
                result = dataContainer.voxels.get(voxelId).getLayerId();
            }
            return result;
        }
    }

    @Override
    public final boolean clearV(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.layers.containsKey(layerId)) {
                if (dataContainer.layers.get(layerId).getSize() > 0) {
                    historyManagerV.applyIntent(new ClearVoxelIntent(this, layerId, false));
                    result = true;
                }
            }
            return result;
        }
    }

    @Override
    public final Voxel searchVoxel(int[] pos, int layerId) {
        synchronized (VitcoSettings.SYNC) {
            return dataContainer.layers.get(layerId).search(pos);
        }
    }

    @Override
    public final Voxel searchVoxel(int[] pos, boolean onlyCurrentLayer) {
        synchronized (VitcoSettings.SYNC) {
            if (onlyCurrentLayer) { // search only the current layers
                VoxelLayer layer = dataContainer.layers.get(dataContainer.selectedLayer);
                if (layer != null && layer.isVisible()) {
                    Voxel result = layer.search(pos);
                    if (result != null) {
                        return result;
                    }
                }
            } else { // search all layers in correct order
                for (Integer layerId : dataContainer.layerOrder) {
                    if (dataContainer.layers.get(layerId).isVisible()) {
                        Voxel result = dataContainer.layers.get(layerId).search(pos);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
            return null;
        }
    }

    // ================================ selection of voxels

    // select a voxel
    @Override
    public final boolean setVoxelSelected(int voxelId, boolean selected) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.voxels.containsKey(voxelId) && dataContainer.voxels.get(voxelId).isSelected() != selected) {
                historyManagerV.applyIntent(new SelectVoxelIntent(this, voxelId, selected, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean isSelected(int voxelId) {
        synchronized (VitcoSettings.SYNC) {
            return dataContainer.voxels.containsKey(voxelId) && dataContainer.voxels.get(voxelId).isSelected();
        }
    }

    private final HashMap<String, TIntObjectHashMap<int[]>> changedSelectedVoxel = new HashMap<String, TIntObjectHashMap<int[]>>();
    @Override
    public final Voxel[][] getNewSelectedVoxel(String requestId) {
        synchronized (VitcoSettings.SYNC) {
            if (!changedSelectedVoxel.containsKey(requestId)) {
                changedSelectedVoxel.put(requestId, null);
            }
            if (changedSelectedVoxel.get(requestId) == null) {
                changedSelectedVoxel.put(requestId, new TIntObjectHashMap<int[]>());
                return new Voxel[][] {null, getSelectedVoxels()};
            } else {
                ArrayList<Voxel> removed = new ArrayList<Voxel>();
                ArrayList<Voxel> added = new ArrayList<Voxel>();
                for (int[] pos : changedSelectedVoxel.get(requestId).valueCollection()) {
                    Voxel voxel = searchVoxel(pos, false);
                    if (voxel != null && voxel.isSelected()) {
                        added.add(voxel);
                    } else {
                        removed.add(new Voxel(-1, pos, null, false, null, -1));
                    }
                }
                Voxel[][] result = new Voxel[2][];
                result[0] = new Voxel[removed.size()];
                removed.toArray(result[0]);
                result[1] = new Voxel[added.size()];
                added.toArray(result[1]);
                changedSelectedVoxel.get(requestId).clear();
                return result;
            }
        }
    }

    // get selected visible voxels
    @Override
    public final Voxel[] getSelectedVoxels() {
        synchronized (VitcoSettings.SYNC) {
            if (!selectedVoxelBufferValid) {
                // get all presented voxels
                Voxel voxels[] = _getVisibleLayerVoxel();
                // filter the selected
                ArrayList<Voxel> selected = new ArrayList<Voxel>();
                for (Voxel voxel : voxels) {
                    if (voxel.isSelected()) {
                        selected.add(voxel);
                    }
                }
                selectedVoxelBuffer = new Voxel[selected.size()];
                selected.toArray(selectedVoxelBuffer);
                selectedVoxelBufferValid = true;
            }
            return selectedVoxelBuffer.clone();
        }
    }

    @Override
    public final boolean massSetVoxelSelected(Integer[] voxelIds, boolean selected) {
        synchronized (VitcoSettings.SYNC) {
            ArrayList<Integer> validVoxel = new ArrayList<Integer>();
            for (Integer voxelId : voxelIds) {
                if (dataContainer.voxels.containsKey(voxelId) && dataContainer.voxels.get(voxelId).isSelected() != selected) {
                    validVoxel.add(voxelId);
                }
            }
            if (validVoxel.size() > 0) {
                Integer[] valid = new Integer[validVoxel.size()];
                validVoxel.toArray(valid);
                historyManagerV.applyIntent(new MassSelectVoxelIntent(this, valid, selected, false));
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public final boolean migrateVoxels(Voxel[] voxels) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (voxels.length > 0 && voxels.length <= VitcoSettings.MAX_VOXEL_COUNT_PER_LAYER) {
                historyManagerV.applyIntent(new MigrateIntent(this, voxels, false));
                result = true;
            }
            return result;
        }
    }


    // =============================

    Voxel[] layerVoxelBuffer = new Voxel[0];
    boolean layerVoxelBufferValid = false;
    int layerVoxelBufferLastLayer;
    @Override
    public final Voxel[] getLayerVoxels(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            if (!layerVoxelBufferValid || layerVoxelBufferLastLayer != layerId) {
                VoxelLayer layer = dataContainer.layers.get(layerId);
                if (layer != null) {
                    layerVoxelBuffer = layer.getVoxels();
                } else {
                    layerVoxelBuffer = new Voxel[0];
                }
                layerVoxelBufferValid = true;
                layerVoxelBufferLastLayer = layerId;
            }
            return layerVoxelBuffer.clone();
        }
    }

    // get the new visible voxels, NOTE: if first element of array is null
    // this means that everything is erased
    private final HashMap<String, TIntObjectHashMap<int[]>> changedVisibleVoxel = new HashMap<String, TIntObjectHashMap<int[]>>();
    @Override
    public final Voxel[][] getNewVisibleLayerVoxel(String requestId) {
        synchronized (VitcoSettings.SYNC) {
            if (!changedVisibleVoxel.containsKey(requestId)) {
                changedVisibleVoxel.put(requestId, null);
            }
            if (changedVisibleVoxel.get(requestId) == null) {
                changedVisibleVoxel.put(requestId, new TIntObjectHashMap<int[]>());
                return new Voxel[][] {null, _getVisibleLayerVoxel()};
            } else {
                ArrayList<Voxel> removed = new ArrayList<Voxel>();
                ArrayList<Voxel> added = new ArrayList<Voxel>();
                for (int[] pos : changedVisibleVoxel.get(requestId).valueCollection()) {
                    Voxel voxel = searchVoxel(pos, false);
                    if (voxel != null) {
                        added.add(voxel);
                    } else {
                        removed.add(new Voxel(-1, pos, null, false, null, -1));
                    }
                }
                Voxel[][] result = new Voxel[2][];
                result[0] = new Voxel[removed.size()];
                removed.toArray(result[0]);
                result[1] = new Voxel[added.size()];
                added.toArray(result[1]);
                changedVisibleVoxel.get(requestId).clear();
                return result;
            }
        }
    }

    // internal function, heavy!
    Voxel[] visibleLayerVoxelInternalBuffer = new Voxel[0];
    boolean visibleLayerVoxelInternalBufferValid = false;
    private Voxel[] _getVisibleLayerVoxel() {
        if (!visibleLayerVoxelInternalBufferValid) {
            VoxelLayer result = new VoxelLayer(-1, "tmp");
            for (Integer layerId : dataContainer.layerOrder) {
                if (dataContainer.layers.get(layerId).isVisible()) {
                    Voxel[] voxels = dataContainer.layers.get(layerId).getVoxels();
                    for (Voxel voxel : voxels) {
                        if (result.voxelPositionFree(voxel)) {
                            result.addVoxel(voxel);
                        }
                    }
                }
            }
            visibleLayerVoxelInternalBuffer = result.getVoxels();
            visibleLayerVoxelInternalBufferValid = true;
        }
        return visibleLayerVoxelInternalBuffer.clone();
    }

    // returns visible voxels
    @Override
    public final Voxel[] getVisibleLayerVoxel() {
        synchronized (VitcoSettings.SYNC) {
            updateVisVoxTreeInternal();
            return visibleLayerVoxelBuffer;
        }
    }

    // helper to update buffers for visible voxels
    private Voxel[] visibleLayerVoxelBuffer = new Voxel[0];
    private boolean anyVoxelsVisibleBuffer = false;
    private final HashMap<String, Voxel> visVoxelList = new HashMap<String, Voxel>();
    private void updateVisVoxTreeInternal() {
        Voxel[][] newV = getNewVisibleLayerVoxel("___internal___visible_list");
        if (newV[0] == null) {
            visVoxelList.clear();
        } else {
            for (Voxel removed : newV[0]) {
                visVoxelList.remove(removed.getPosAsString());
            }
        }
        for (Voxel added : newV[1]) {
            visVoxelList.put(added.getPosAsString(), added);
        }
        // update the buffer
        if (newV[0]== null || newV[0].length > 0 || newV[1].length > 0) {
            visibleLayerVoxelBuffer = new Voxel[visVoxelList.size()];
            visVoxelList.values().toArray(visibleLayerVoxelBuffer);
            anyVoxelsVisibleBuffer = visibleLayerVoxelBuffer.length > 0;
        }
    }

    // true iff any voxel are visible
    @Override
    public final boolean anyLayerVoxelVisible() {
        synchronized (VitcoSettings.SYNC) {
            updateVisVoxTreeInternal();
            return anyVoxelsVisibleBuffer;
        }
    }

    // true iff any voxel are selected
    @Override
    public final boolean anyVoxelSelected() {
        synchronized (VitcoSettings.SYNC) {
            if (!selectedVoxelBufferValid) {
                return getSelectedVoxels().length > 0;
            } else {
                return selectedVoxelBuffer.length > 0;
            }
        }
    }

    // to invalidate the side view buffer
    @Override
    public final void invalidateSideViewBuffer(String requestId, Integer side, Integer plane) {
        synchronized (VitcoSettings.SYNC) {
            // make sure this plane is set
            if (!changedVisibleVoxelPlane.containsKey(side)) {
                changedVisibleVoxelPlane.put(side, new HashMap<String, TIntObjectHashMap<TIntObjectHashMap<int[]>>>());
            }
            // make sure the requestId is set
            if (!changedVisibleVoxelPlane.get(side).containsKey(requestId)) {
                changedVisibleVoxelPlane.get(side).put(requestId, new TIntObjectHashMap<TIntObjectHashMap<int[]>>());
            }
            // make sure this plane has no information stored (force complete refresh)
            changedVisibleVoxelPlane.get(side).get(requestId).remove(plane);
        }
    }

    // side -> requestId -> plane -> positions
    private final TIntObjectHashMap<HashMap<String, TIntObjectHashMap<TIntObjectHashMap<int[]>>>> changedVisibleVoxelPlane
            = new TIntObjectHashMap<HashMap<String, TIntObjectHashMap<TIntObjectHashMap<int[]>>>>();
    @Override
    public final Voxel[][] getNewSideVoxel(String requestId, Integer side, Integer plane) {
        synchronized (VitcoSettings.SYNC) {
            // default result (delete all + empty)
            Voxel[][] result = new Voxel[][]{null, new Voxel[0]};
            // make sure this plane is set
            if (!changedVisibleVoxelPlane.containsKey(side)) {
                changedVisibleVoxelPlane.put(side, new HashMap<String, TIntObjectHashMap<TIntObjectHashMap<int[]>>>());
            }
            // make sure the requestId is set
            if (!changedVisibleVoxelPlane.get(side).containsKey(requestId)) {
                changedVisibleVoxelPlane.get(side).put(requestId, new TIntObjectHashMap<TIntObjectHashMap<int[]>>());
            }

            if (changedVisibleVoxelPlane.get(side).get(requestId).get(plane) == null) {
                // if the plane is null, fetch all data and set it no empty
                switch (side) {
                    case 0:
                        result = new Voxel[][] {null, getVoxelsXY(plane)};
                        break;
                    case 1:
                        result = new Voxel[][] {null, getVoxelsXZ(plane)};
                        break;
                    case 2:
                        result = new Voxel[][] {null, getVoxelsYZ(plane)};
                        break;
                    default: break;
                }
                // reset
                changedVisibleVoxelPlane.get(side).get(requestId).put(plane, new TIntObjectHashMap<int[]>());
            } else {
                // if there are changed positions, notify only those positions
                ArrayList<Voxel> removed = new ArrayList<Voxel>();
                ArrayList<Voxel> added = new ArrayList<Voxel>();
                for (int[] pos : changedVisibleVoxelPlane.get(side).get(requestId).get(plane).valueCollection()) {
                    Voxel voxel = searchVoxel(pos, false);
                    if (voxel != null) {
                        added.add(voxel);
                    } else {
                        removed.add(new Voxel(-1, pos, null, false, null, -1));
                    }
                }
                result = new Voxel[2][];
                result[0] = new Voxel[removed.size()];
                removed.toArray(result[0]);
                result[1] = new Voxel[added.size()];
                added.toArray(result[1]);
                // these changes have now been forwarded
                changedVisibleVoxelPlane.get(side).get(requestId).get(plane).clear();
            }
            // return the result
            return result;
        }
    }

    @Override
    public final Voxel[] getVoxelsXY(int z, int layerId) {
        synchronized (VitcoSettings.SYNC) {
            if (dataContainer.layers.containsKey(layerId)) {
                return dataContainer.layers.get(layerId).getZPlane(z);
            }
            return null;
        }
    }

    @Override
    public final Voxel[] getVoxelsXZ(int y, int layerId) {
        synchronized (VitcoSettings.SYNC) {
            if (dataContainer.layers.containsKey(layerId)) {
                return dataContainer.layers.get(layerId).getYPlane(y);
            }
            return null;
        }
    }

    @Override
    public final Voxel[] getVoxelsYZ(int x, int layerId) {
        synchronized (VitcoSettings.SYNC) {
            if (dataContainer.layers.containsKey(layerId)) {
                return dataContainer.layers.get(layerId).getXPlane(x);
            }
            return null;
        }
    }

    int lastVoxelXYBufferZValue;
    boolean layerVoxelXYBufferValid = false;
    Voxel[] layerVoxelXYBuffer = new Voxel[0];
    @Override
    public final Voxel[] getVoxelsXY(int z) {
        synchronized (VitcoSettings.SYNC) {
            if (!layerVoxelXYBufferValid || z != lastVoxelXYBufferZValue) {

                VoxelLayer result = new VoxelLayer(-1, "tmp");
                for (Integer layerId : dataContainer.layerOrder) {
                    if (dataContainer.layers.get(layerId).isVisible()) {
                        Voxel[] voxels = dataContainer.layers.get(layerId).getZPlane(z);
                        for (Voxel voxel : voxels) {
                            if (result.voxelPositionFree(voxel)) {
                                result.addVoxel(voxel);
                            }
                        }
                    }
                }
                layerVoxelXYBuffer = result.getVoxels();
                layerVoxelXYBufferValid = true;
                lastVoxelXYBufferZValue = z;
            }
            return layerVoxelXYBuffer.clone();
        }
    }

    int lastVoxelXZBufferYValue;
    boolean layerVoxelXZBufferValid = false;
    Voxel[] layerVoxelXZBuffer = new Voxel[0];
    @Override
    public final Voxel[] getVoxelsXZ(int y) {
        synchronized (VitcoSettings.SYNC) {
            if (!layerVoxelXZBufferValid || y != lastVoxelXZBufferYValue) {

                VoxelLayer result = new VoxelLayer(-1, "tmp");
                for (Integer layerId : dataContainer.layerOrder) {
                    if (dataContainer.layers.get(layerId).isVisible()) {
                        Voxel[] voxels = dataContainer.layers.get(layerId).getYPlane(y);
                        for (Voxel voxel : voxels) {
                            if (result.voxelPositionFree(voxel)) {
                                result.addVoxel(voxel);
                            }
                        }
                    }
                }
                layerVoxelXZBuffer = result.getVoxels();
                layerVoxelXZBufferValid = true;
                lastVoxelXZBufferYValue = y;
            }
            return layerVoxelXZBuffer.clone();
        }
    }

    int lastVoxelYZBufferXValue;
    boolean layerVoxelYZBufferValid = false;
    Voxel[] layerVoxelYZBuffer = new Voxel[0];
    @Override
    public final Voxel[] getVoxelsYZ(int x) {
        synchronized (VitcoSettings.SYNC) {
            if (!layerVoxelYZBufferValid || x != lastVoxelYZBufferXValue) {

                VoxelLayer result = new VoxelLayer(-1, "tmp");
                for (Integer layerId : dataContainer.layerOrder) {
                    if (dataContainer.layers.get(layerId).isVisible()) {
                        Voxel[] voxels = dataContainer.layers.get(layerId).getXPlane(x);
                        for (Voxel voxel : voxels) {
                            if (result.voxelPositionFree(voxel)) {
                                result.addVoxel(voxel);
                            }
                        }
                    }
                }
                layerVoxelYZBuffer = result.getVoxels();
                layerVoxelYZBufferValid = true;
                lastVoxelYZBufferXValue = x;
            }
            return layerVoxelYZBuffer.clone();
        }
    }

    @Override
    public final int getVoxelCount(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            int result = 0;
            if (dataContainer.layers.containsKey(layerId)) {
                result = dataContainer.layers.get(layerId).getSize();
            }
            return result;
        }
    }

    // ==================================

    @Override
    public final void undoV() {
        synchronized (VitcoSettings.SYNC) {
            historyManagerV.unapply();
        }
    }

    @Override
    public final void redoV() {
        synchronized (VitcoSettings.SYNC) {
            historyManagerV.apply();
        }
    }

    @Override
    public final boolean canUndoV() {
        synchronized (VitcoSettings.SYNC) {
            return historyManagerV.canUndo();
        }
    }

    @Override
    public final boolean canRedoV() {
        synchronized (VitcoSettings.SYNC) {
            return historyManagerV.canRedo();
        }
    }

    @Override
    public final int createLayer(String layerName) {
        synchronized (VitcoSettings.SYNC) {
            int layerId = getFreeLayerId();
            historyManagerV.applyIntent(new CreateLayerIntent(this, layerId, layerName, false));
            notifier.onLayerStateChanged();
            return layerId;
        }
    }

    @Override
    public  final boolean deleteLayer(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.layers.containsKey(layerId)) {
                historyManagerV.applyIntent(new DeleteLayerIntent(this, layerId, false));
                notifier.onLayerStateChanged();
                result = true;
            }
            return result;
        }
    }

    @Override
    public  final boolean renameLayer(int layerId, String newName) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.layers.containsKey(layerId) && !newName.equals(dataContainer.layers.get(layerId).getName())) {
                historyManagerV.applyIntent(new RenameLayerIntent(this, layerId, newName, false));
                notifier.onLayerStateChanged();
                result = true;
            }
            return result;
        }
    }

    @Override
    public final String getLayerName(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            return dataContainer.layers.containsKey(layerId) ? dataContainer.layers.get(layerId).getName() : null;
        }
    }

    private boolean layerNameBufferValid = false;
    private String[] layerNameBuffer = new String[]{};
    @Override
    public final String[] getLayerNames() {
        synchronized (VitcoSettings.SYNC) {
            if (!layerNameBufferValid) {
                if (layerNameBuffer.length != dataContainer.layers.size()) {
                    layerNameBuffer = new String[dataContainer.layers.size()];
                }
                int i = 0;
                for (Integer layerId : dataContainer.layerOrder) {
                    layerNameBuffer[i++] = getLayerName(layerId);
                }
                layerNameBufferValid = true;
            }
            return layerNameBuffer.clone();
        }
    }

    @Override
    public final boolean selectLayer(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if ((dataContainer.layers.containsKey(layerId) || layerId == -1) && dataContainer.selectedLayer != layerId) {
                historyManagerV.applyIntent(new SelectLayerIntent(this, layerId, false));
                notifier.onLayerStateChanged();
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean selectLayerSoft(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if ((dataContainer.layers.containsKey(layerId) || layerId == -1) && dataContainer.selectedLayer != layerId) {
                dataContainer.selectedLayer = layerId;
                invalidateV(new int[0][]);
                notifier.onLayerStateChanged();
                result = true;
            }
            return result;
        }
    }

    @Override
    public final int getSelectedLayer() {
        synchronized (VitcoSettings.SYNC) {
            // make sure the selected layer is always valid
            return dataContainer.layers.containsKey(dataContainer.selectedLayer) ? dataContainer.selectedLayer : -1;
        }
    }

    private boolean layerBufferValid = false;
    private Integer[] layerBuffer = new Integer[]{};
    @Override
    public final Integer[] getLayers() {
        synchronized (VitcoSettings.SYNC) {
            if (!layerBufferValid) {
                if (layerBuffer.length != dataContainer.layers.size()) {
                    layerBuffer = new Integer[dataContainer.layers.size()];
                }
                dataContainer.layerOrder.toArray(layerBuffer);
                layerBufferValid = true;
            }
            return layerBuffer.clone();
        }
    }

    @Override
    public final boolean setVisible(int layerId, boolean b) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.layers.containsKey(layerId) && dataContainer.layers.get(layerId).isVisible() != b) {
                historyManagerV.applyIntent(new LayerVisibilityIntent(this, layerId, b, false));
                notifier.onLayerStateChanged();
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean getLayerVisible(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.layers.containsKey(layerId)) {
                result = dataContainer.layers.get(layerId).isVisible();
            }
            return result;
        }
    }

    @Override
    public final boolean moveLayerUp(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (canMoveLayerUp(layerId)) {
                historyManagerV.applyIntent(new MoveLayerIntent(this, layerId, true, false));
                notifier.onLayerStateChanged();
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean moveLayerDown(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (canMoveLayerDown(layerId)) {
                historyManagerV.applyIntent(new MoveLayerIntent(this, layerId, false, false));
                notifier.onLayerStateChanged();
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean canMoveLayerUp(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            return dataContainer.layers.containsKey(layerId) && dataContainer.layerOrder.lastIndexOf(layerId) > 0;
        }
    }

    @Override
    public final boolean canMoveLayerDown(int layerId) {
        synchronized (VitcoSettings.SYNC) {
            return dataContainer.layers.containsKey(layerId) && dataContainer.layerOrder.lastIndexOf(layerId) < dataContainer.layerOrder.size() - 1;
        }
    }

    @Override
    public final boolean mergeVisibleLayers() {
        synchronized (VitcoSettings.SYNC) {
            if (canMergeVisibleLayers()) {
                historyManagerV.applyIntent(new MergeLayersIntent(this, false));
                notifier.onLayerStateChanged();
                return true;
            }
            return false;
        }
    }

    @Override
    public final boolean canMergeVisibleLayers() {
        synchronized (VitcoSettings.SYNC) {
            // if there are more than one visible layer
            int visibleLayers = 0;
            for (int layerId : dataContainer.layerOrder) {
                if (dataContainer.layers.get(layerId).isVisible()) {
                    if (visibleLayers > 0) {
                        return true;
                    }
                    visibleLayers++;
                }
            }
            return false;
        }
    }

    // texture actions

    @Override
    public final void addTexture(BufferedImage image) {
        synchronized (VitcoSettings.SYNC) {
            // make sure that the graphic is a mutiple of 32
            int width = ((int)Math.ceil(image.getWidth() / 32f)) * 32;
            int height = ((int)Math.ceil(image.getHeight() / 32f)) * 32;

            BufferedImage texture;
            if (width != image.getWidth() || height != image.getHeight()) {
                texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                texture.getGraphics().drawImage(image, 0, 0, null);
            } else {
                texture = GraphicTools.deepCopy(image);
            }
            historyManagerV.applyIntent(new AddTextureGridIntent(this, texture, false));
        }
    }

    @Override
    public final boolean removeTexture(int textureId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            // check that this texture is not used (return false if used)
            for (Voxel voxel : dataContainer.voxels.values()) {
                if (ArrayUtil.contains(voxel.getTexture(), textureId)) {
                    return false;
                }
            }
            if (dataContainer.textures.containsKey(textureId)) {
                historyManagerV.applyIntent(new RemoveTextureIntent(this, textureId, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean removeAllTexture() {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.textures.size() > 0) {

                // check which textures are not in use
                ArrayList<Integer> unusedTextures = new ArrayList<Integer>(dataContainer.textures.keySet());
                for (Voxel voxel : dataContainer.voxels.values()) {
                    int[] textures = voxel.getTexture();
                    if (textures != null) {
                        for (Integer textureId : textures) {
                            unusedTextures.remove(textureId);
                        }
                    }
                }
                if (unusedTextures.size() > 0) {
                    historyManagerV.applyIntent(new RemoveAllTextureIntent(this, unusedTextures, false));
                    result = true;
                }
            }
            return result;
        }
    }

    @Override
    public final boolean replaceTexture(int textureId, ImageIcon texture) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.textures.containsKey(textureId) &&
                    texture.getIconWidth() == 32 && texture.getIconHeight() == 32) {
                historyManagerV.applyIntent(new ReplaceTextureIntent(this, textureId, texture, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final Integer[] getTextureList() {
        synchronized (VitcoSettings.SYNC) {
            Integer[] result = new Integer[dataContainer.textures.size()];
            dataContainer.textures.keySet().toArray(result);
            return result;
        }
    }

    @Override
    public final TIntHashSet getVoxelColorList() {
        synchronized (VitcoSettings.SYNC) {
            TIntHashSet result = new TIntHashSet();
            for (Integer layerId : getLayers()) {
                result.addAll(dataContainer.layers.get(layerId).getVoxelColors());
            }
            return result;
        }
    }

    @Override
    public final ImageIcon getTexture(Integer textureId) {
        synchronized (VitcoSettings.SYNC) {
            Image internalImg = dataContainer.textures.get(textureId).getImage();
            BufferedImage result = new BufferedImage(
                    internalImg.getWidth(null), internalImg.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);
            result.getGraphics().drawImage(internalImg, 0, 0, null);
            return new ImageIcon(result);
        }
    }

    @Override
    public final String getTextureHash(Integer textureId) {
        synchronized (VitcoSettings.SYNC) {
            if (dataContainer.textures.containsKey(textureId)) {
                if (dataContainer.textures.get(textureId).getDescription() == null) {
                    ImageIcon img = dataContainer.textures.get(textureId);
                    BufferedImage bi = new BufferedImage(
                            img.getIconWidth(),img.getIconHeight(),BufferedImage.TYPE_INT_RGB);
                    bi.createGraphics().drawImage(img.getImage(),0,0,null);
                    dataContainer.textures.get(textureId).setDescription(GraphicTools.getHash(bi));
                }
                return dataContainer.textures.get(textureId).getDescription();
            } else {
                return "";
            }
        }
    }

    @Override
    public final void selectTexture(int textureId) {
        synchronized (VitcoSettings.SYNC) {
            if (textureId != -1 && dataContainer.textures.containsKey(textureId)) {
                if (textureId != dataContainer.selectedTexture) {
                    historyManagerV.applyIntent(new SelectTextureIntent(this, textureId, false));
                }
            } else {
                if (dataContainer.selectedTexture != -1) {
                    historyManagerV.applyIntent(new SelectTextureIntent(this, -1, false));
                }
            }
        }
    }

    @Override
    public final void selectTextureSoft(int textureId) {
        synchronized (VitcoSettings.SYNC) {
            if (dataContainer.selectedTexture != textureId &&
                    (textureId == -1 || dataContainer.textures.containsKey(textureId))) {
                dataContainer.selectedTexture = textureId;
                notifier.onTextureDataChanged();
            }
        }
    }

    @Override
    public final int getSelectedTexture() {
        synchronized (VitcoSettings.SYNC) {
            if (!dataContainer.textures.containsKey(dataContainer.selectedTexture)) {
                selectTextureSoft(-1);
            }
            return dataContainer.selectedTexture;
        }
    }

    @Override
    public final boolean setTexture(int voxelId, int voxelSide, int textureId) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.voxels.containsKey(voxelId) &&
                    (dataContainer.voxels.get(voxelId).getTexture() == null ||
                    dataContainer.voxels.get(voxelId).getTexture()[voxelSide] != textureId)) {
                historyManagerV.applyIntent(new TextureVoxelIntent(this, voxelId, voxelSide, textureId, false));
                result = true;
            }
            return result;
        }
    }

    @Override
    public final boolean massSetTexture(Integer[] voxelIds, int textureId) {
        synchronized (VitcoSettings.SYNC) {
            ArrayList<Integer> validVoxel = new ArrayList<Integer>();
            for (int voxelId : voxelIds) {
                if (dataContainer.voxels.containsKey(voxelId)) {
                    validVoxel.add(voxelId);
                }
            }
            if (validVoxel.size() > 0) {
                Integer[] valid = new Integer[validVoxel.size()];
                validVoxel.toArray(valid);
                historyManagerV.applyIntent(new MassTextureVoxelIntent(this, valid, textureId, false));
                return true;
            } else {
                return false;
            }
        }
    }

    // get texture id of a voxel
    @Override
    public final int[] getVoxelTextureIds(int voxelId) {
        synchronized (VitcoSettings.SYNC) {
            if (dataContainer.voxels.containsKey(voxelId)) {
                return dataContainer.voxels.get(voxelId).getTexture();
            }
            return null; // error
        }
    }

    // flip the texture of a voxel
    @Override
    public final boolean flipVoxelTexture(int voxelId, int voxelSide) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.voxels.containsKey(voxelId) &&
                    dataContainer.voxels.get(voxelId).getTexture() != null) {
                historyManagerV.applyIntent(new FlipVoxelTextureIntent(this, voxelId, voxelSide, false));
                result = true;
            }
            return result;
        }
    }

    // rotate the texture of a voxel
    @Override
    public final boolean rotateVoxelTexture(int voxelId, int voxelSide) {
        synchronized (VitcoSettings.SYNC) {
            boolean result = false;
            if (dataContainer.voxels.containsKey(voxelId) &&
                    dataContainer.voxels.get(voxelId).getTexture() != null) {
                historyManagerV.applyIntent(new RotateVoxelTextureIntent(this, voxelId, voxelSide, false));
                result = true;
            }
            return result;
        }
    }
}
