package _2no2name.worldthreader.util;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;

@Deprecated
public record CrashInfo(ServerWorld world, Throwable throwable) {

	public void crash(String title) {
		CrashReport report = CrashReport.create(this.throwable, title);
		this.world.addDetailsToCrashReport(report);
		throw new CrashException(report);
	}
}
