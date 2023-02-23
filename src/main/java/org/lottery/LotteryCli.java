package org.lottery;

import org.kohsuke.github.GHPermissionType;

import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.annotations.Command;

import io.quarkiverse.githubapp.command.airline.Permission;
import io.quarkus.arc.Arc;

import java.io.IOException;

@Cli(name = "/lottery", commands = { LotteryCli.DrawCommand.class })
public class LotteryCli {

    interface Commands {
        void run() throws Exception;
    }

    @Command(name = "draw")
    @Permission(GHPermissionType.ADMIN)
    static class DrawCommand implements Commands {
        @Override
        public void run() throws Exception {
            // Cannot inject the service for some reason,
            // as Airline uses reflection and performs calls to setAccessible recursively.
            Arc.container().instance(LotteryService.class).get().commentOnIssue();
        }
    }
}
