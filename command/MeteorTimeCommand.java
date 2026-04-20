package org.axial.prisonsCore.command;

import org.axial.prisonsCore.service.MeteorService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MeteorTimeCommand implements CommandExecutor {
    private final MeteorService meteorService;

    public MeteorTimeCommand(MeteorService meteorService) {
        this.meteorService = meteorService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("prisons.meteor.time")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        long nextAt = this.meteorService.getNextMeteorAtMillis();
        if (nextAt <= 0L) {
            sender.sendMessage(Text.color("&6&l(!) Next Meteor:"));
            sender.sendMessage(Text.color("&cMeteor timing is not currently running."));
            return true;
        }
        sender.sendMessage(Text.color("&6&l(!) Next Meteor:"));
        sender.sendMessage(Text.color("&fIn &e" + this.meteorService.formatDuration(this.meteorService.getTimeUntilNextMeteorMillis())));
        return true;
    }
}
