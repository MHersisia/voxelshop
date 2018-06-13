package com.vitco.app.core.data;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

// flip voxel texture
final class FlipVoxelTextureIntent extends VoxelActionIntent  {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Voxel voxel;
    private final int voxelSide;

    protected FlipVoxelTextureIntent(VoxelData voxelData, int voxelId, int voxelSide, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.voxel = this.voxelData.dataContainer.voxels.get(voxelId);
        this.voxelSide = voxelSide;
        effected = new int[][]{voxel.getPosAsInt()};
    }

    @Override
    protected void applyAction() {
        voxel.flip(voxelSide);
    }

    @Override
    protected void unapplyAction() {
        voxel.flip(voxelSide);
    }

    private int[][] effected = null;
    @Override
    public int[][] effected() {
        return effected;
    }
}