package com.vitco.app.core.data;

import java.awt.Color;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

// voxel intents
final class AddVoxelIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Voxel voxel;

    protected AddVoxelIntent(VoxelData voxelData, int voxelId, int[] pos, Color color, boolean selected,
                             int[] textureId, int layerId, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        voxel = new Voxel(voxelId, pos, color, selected, textureId, layerId);
    }

    @Override
    protected void applyAction() {
        this.voxelData.dataContainer.voxels.put(voxel.id, voxel);
        this.voxelData.dataContainer.layers.get(voxel.getLayerId()).addVoxel(voxel);
    }

    @Override
    protected void unapplyAction() {
        this.voxelData.dataContainer.voxels.remove(voxel.id);
        this.voxelData.dataContainer.layers.get(voxel.getLayerId()).removeVoxel(voxel);
    }

    @Override
    public int[][] effected() {
        return new int[][]{voxel.getPosAsInt()};
    }
}