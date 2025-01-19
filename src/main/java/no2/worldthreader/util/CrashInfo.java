package no2.worldthreader.util;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.server.level.ServerLevel;

@Deprecated
public record CrashInfo(ServerLevel world, Throwable throwable) {

	public void crash(String title) {
		CrashReport report = CrashReport.forThrowable(this.throwable, title);
		this.world.fillReportDetails(report);
		throw new ReportedException(report);
	}
}
