package com.vitco.app.core.data;

import java.util.Collections;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

final class MoveLayerIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Integer layerId;
    private final boolean moveUp;

    protected MoveLayerIntent(VoxelData voxelData, int layerId, boolean moveUp, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.layerId = layerId;
        this.moveUp = moveUp;
    }

    @Override
    protected void applyAction() {
        int index = this.voxelData.dataContainer.layerOrder.lastIndexOf(layerId);
        if (moveUp) {
            Collections.swap(this.voxelData.dataContainer.layerOrder, index, index - 1);
        } else {
            Collections.swap(this.voxelData.dataContainer.layerOrder, index, index + 1);
        }
    }

    @Override
    protected void unapplyAction() {
        int index = this.voxelData.dataContainer.layerOrder.lastIndexOf(layerId);
        if (moveUp) {
            Collections.swap(this.voxelData.dataContainer.layerOrder, index, index + 1);
        } else {
            Collections.swap(this.voxelData.dataContainer.layerOrder, index, index - 1);
        }
    }

    private int[][] effected = null; // everything effected
    @Override
    public int[][] effected() {
        if (effected == null) { // get effected positions
            Voxel[] voxels = this.voxelData.dataContainer.layers.get(layerId).getVoxels();
            effected = new int[voxels.length][];
            for (int i = 0, voxelsLength = voxels.length; i < voxelsLength; i++) {
                effected[i] = voxels[i].getPosAsInt();
            }
        }
        return effected;
    }
}