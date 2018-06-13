package com.vitco.app.core.data;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

final class MassMoveVoxelIntent extends VoxelActionIntent  {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Voxel[] voxels;
    private final int[] shift;

    protected MassMoveVoxelIntent(VoxelData voxelData, Voxel[] voxels, int[] shift, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.voxels = voxels;
        this.shift = shift;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {

            // what is effected (there could be duplicate positions here)
            effected = new int[voxels.length*2][];

            // generate the ids
            Integer[] voxelIds = new Integer[voxels.length];
            for (int i = 0; i < voxels.length; i++) {
                voxelIds[i] = voxels[i].id;
            }
            // remove all voxels
            this.voxelData.historyManagerV.applyIntent(new MassRemoveVoxelIntent(this.voxelData, voxelIds, true));

            // create new voxels (with new position) and delete
            // existing voxels at those positions
            Voxel[] shiftedVoxels = new Voxel[voxels.length];
            for (int i = 0; i < voxels.length; i++) {
                Voxel voxel = voxels[i];
                int[] pos = voxel.getPosAsInt();
                effected[i] = voxel.getPosAsInt(); // what is effected
                pos[0] -= shift[0];
                pos[1] -= shift[1];
                pos[2] -= shift[2];
                effected[i + voxels.length] = pos.clone(); // what is effected
                shiftedVoxels[i] = new Voxel(voxel.id, pos, voxel.getColor(), voxel.isSelected(), voxel.getTexture(), voxel.getLayerId());
                // remove existing voxels in this layer
                Voxel result = this.voxelData.dataContainer.layers.get(voxel.getLayerId()).search(pos);
                if (result != null) {
                    this.voxelData.historyManagerV.applyIntent(new RemoveVoxelIntent(this.voxelData, result.id, true));
                }
            }
            // (re)add all the shifted voxels (null ~ the voxel layer id is used)
            this.voxelData.historyManagerV.applyIntent(new MassAddVoxelIntent(this.voxelData, shiftedVoxels, null, true));
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