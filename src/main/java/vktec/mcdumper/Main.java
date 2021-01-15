package vktec.mcdumper;

import com.google.common.collect.ImmutableList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.NetworkState;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.util.registry.Registry;

public class Main {
	public static void main(String[] args) throws IOException {
		dumpPackets();
		dumpBlocks();
	}

	private static void dumpPackets() throws IOException {
		FileWriter file = new FileWriter("packets.txt");
		Formatter f = new Formatter(file);
		f.format("# generated by mcdumper, protocol version %d\n", SharedConstants.getGameVersion().getProtocolVersion());

		for (NetworkState state : NetworkState.class.getEnumConstants()) {
			for (NetworkSide side : NetworkSide.class.getEnumConstants()) {
				if (state == NetworkState.HANDSHAKING && side == NetworkSide.CLIENTBOUND) continue;
				printPackets(f, state, side);
			}
		}

		file.close();
	}

	private static void printPackets(Formatter f, NetworkState state, NetworkSide side) {
		for (int i = 0;; i++) {
			Packet p;
			try {
				p = state.getPacketHandler(side, i);
			} catch (IndexOutOfBoundsException e) {
				break;
			}
			String name = p.getClass().getName()
				.replaceFirst("^.*\\.", "")
				.replaceFirst("(C2S|S2C)Packet(\\$|$)", "")
				.replaceAll("[A-Z]", "_$0")
				.replaceFirst("^_", "")
				.toUpperCase();
			f.format("%s %s %s %d\n", state.name(), side.name(), name, i);
		}
	}

	private static void dumpBlocks() throws IOException {
		FileWriter file = new FileWriter("blocks.txt");
		Formatter f = new Formatter(file);
		f.format("# generated by mcdumper, minecraft version %s\n", SharedConstants.getGameVersion().getName());
		for (Block block : Registry.BLOCK) {
			ImmutableList<BlockState> states = block.getStateManager().getStates();
			f.format("%s %d %d\n", Registry.BLOCK.getId(block), Block.getRawIdFromState(states.get(0)), states.size());
		}
		file.close();
	}
}
