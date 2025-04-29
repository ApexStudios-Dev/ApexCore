package dev.apexstudios.apexcore.lib.component;

public interface Component<
        TBase extends Component<TBase, TObj, THolder, TType>,
        TObj,
        THolder extends ComponentHolder<TBase, TObj, THolder, TType>,
        TType extends ComponentType<TBase, ? extends TBase, TObj, THolder, TType, ?>
> extends ComponentHolder<TBase, TObj, THolder, TType> { }
