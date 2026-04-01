package at.redi2go.photonics.core.config;

import java.util.Optional;

class PhConfigOwner implements Variable.Owner {
    final static PhConfigOwner INSTANCE = new PhConfigOwner();

    private PhConfigOwner() { }

    @Override
    public int mod() {
        return PhConfig.mod;
    }

    @Override
    public <T> Optional<T> getValue(Variable.Type<T> type, String name) {
        return PhConfig.getDefine(type, name);
    }
}
