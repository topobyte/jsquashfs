package com.fernsroth.squashfs;

/**
 * main entry point.
 */
public class Main {
    /**
     * main entry point.
     * @param args the command line arguments.
     * @throws Exception 
     */
    public static void main(String args[]) throws Exception {
        if (args.length > 0) {
            String cmd = args[0];
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
            if (cmd.equals("squash")) {
                Squashfs.main(newArgs);
                return;
            }

            else if (cmd.equals("unsquash")) {
                Unsquashfs.main(newArgs);
                return;
            }
        }

        System.err.println("usage: java -jar jsquashfs.jar [squash|unsquash]");
    }
}
