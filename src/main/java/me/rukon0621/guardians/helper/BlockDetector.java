package me.rukon0621.guardians.helper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class BlockDetector {
    private List<Block> detectedBlock;
    private List<Material> detectedMaterials;
    private List<Block> targetedBlocks;
    public BlockDetector(Location origin, double radius, ArrayList<Material> targetMater) {
        detectedBlock = new ArrayList<Block>();
        detectedMaterials = new ArrayList<>();
        targetedBlocks = new ArrayList<>();
        double x = origin.getX()-radius;
        double y = origin.getY()-radius;
        double z = origin.getZ()-radius;
        for(double dx = x;dx <= origin.getX()+radius;dx++) {
            for(double dy = y;dy <= origin.getY()+radius;dy++) {
                for(double dz = z;dz <= origin.getZ()+radius;dz++) {
                    Location loc = new Location(origin.getWorld(), dx, dy, dz);
                    Block block = loc.getBlock();
                    detectedBlock.add(block);
                    detectedMaterials.add(block.getType());
                    if(targetMater.contains(block.getType())) {
                        targetedBlocks.add(block);
                    }
                }
            }
        }
    }
    public boolean isDetected(Material material) {
        return detectedMaterials.contains(material);
    }
    public int numberOfDetectedMaterial(Material material) {
        int num = 0;
        for(Material mat : detectedMaterials) {
            if(mat==material) num++;
        }
        return num;
    }

    public ArrayList<Block> getTargetBlock() {
        return (ArrayList<Block>) targetedBlocks;
    }

}
