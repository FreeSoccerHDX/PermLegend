package de.freesoccerhdx.lib.command;

public abstract class TypeArgument extends ArgumentTile {


    public TypeArgument(String helpPlaceHolder) {
        super(helpPlaceHolder);
    }

    public abstract boolean checkArgument(String arg);


    public static class BooleanArgument extends TypeArgument {

        public BooleanArgument(String helpPlaceHolder) {
            super(helpPlaceHolder);
        }

        @Override
        public boolean checkArgument(String arg) {
            return arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false");
        }

        @Override
        public String[] getValidArguments() {
            return new String[]{"true", "false"};
        }

    }

    public static class DoubleArgument extends TypeArgument {

        public DoubleArgument(String helpPlaceHolder) {
            super(helpPlaceHolder);
        }

        @Override
        public boolean checkArgument(String arg) {
            try {
                Double.parseDouble(arg);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public String[] getValidArguments() {
            return new String[]{"0.0"};
        }

    }

    public static class IntArgument extends TypeArgument {

        public IntArgument(String helpPlaceHolder) {
            super(helpPlaceHolder);
        }

        @Override
        public boolean checkArgument(String arg) {
            try {
                Integer.parseInt(arg);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public String[] getValidArguments() {
            return new String[]{"1"};
        }

    }

    public static class IntRangeArgument extends TypeArgument {

        private int min;
        private int max;

        public IntRangeArgument(String helpPlaceHolder, int min, int max) {
            super(helpPlaceHolder);
            this.min = min;
            this.max = max;
        }

        @Override
        public String[] getValidArguments() {
            return new String[]{"" + min, "" + max};
        }

        @Override
        public boolean checkArgument(String arg) {
            try {
                int id = Integer.parseInt(arg);
                return id >= min && id <= max;
            } catch (NumberFormatException e) {
            }
            return false;
        }
    }

}
