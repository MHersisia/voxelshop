package com.vitco.app.core.data;

import java.util.ArrayList;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

final class MergeLayersIntent extends VoxelActionIntent {

    /**
	 * 
	 */
	private final VoxelData voxelData;
	protected MergeLayersIntent(VoxelData voxelData, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            ArrayList<int[]> effected = new ArrayList<int[]>();

            // create new layer
            int mergedLayerId = this.voxelData.getFreeLayerId();
            this.voxelData.historyManagerV.applyIntent(new CreateLayerIntent(this.voxelData, mergedLayerId, "Merged", true));

            // add the voxels to the new layer (top to bottom)
            for (int layerId : this.voxelData.dataContainer.layerOrder) {
                if (this.voxelData.dataContainer.layers.get(layerId).isVisible()) { // only visible
                    Voxel[] voxels = this.voxelData.getLayerVoxels(layerId); // get voxels
                    for (Voxel voxel : voxels) {
                        if (this.voxelData.dataContainer.layers.get(mergedLayerId).voxelPositionFree(voxel)) { // add if this voxel does not exist
                            effected.add(voxel.getPosAsInt());
                            this.voxelData.historyManagerV.applyIntent( // we <need> a new id for this voxel
                                    new AddVoxelIntent(this.voxelData, this.voxelData.getFreeVoxelId(), voxel.getPosAsInt(),
                                            voxel.getColor(), voxel.isSelected(), voxel.getTexture(), mergedLayerId, true)
                            );
                        }
                    }
                }
            }

            // delete the visible layers (not the new one)
            Integer[] layer = new Integer[this.voxelData.dataContainer.layerOrder.size()];
            this.voxelData.dataContainer.layerOrder.toArray(layer);
            for (int layerId : layer) {
                if (layerId != mergedLayerId && this.voxelData.dataContainer.layers.get(layerId).isVisible()) {
                    this.voxelData.historyManagerV.applyIntent(new DeleteLayerIntent(this.voxelData, layerId, true));
                }
            }

            // select the new layer (only when created)
            this.voxelData.dataContainer.selectedLayer = mergedLayerId;

            // what is effected
            this.effected = new int[effected.size()][];
            effected.toArray(this.effected);
        }
    }

    @Override
    protected void unapplyAction() {
        // nothing to do
    }

    private int[][] effected = null;
    @Override
    public int[][] effected() {
        return effected;
    }
}