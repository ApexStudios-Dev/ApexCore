package dev.apexstudios.apexcore.lib.component;

import com.google.common.base.Suppliers;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ComponentType<
        TBase extends Component<TBase, TObj, THolder, TType>,
        TComponent extends TBase,
        TObj,
        THolder extends ComponentHolder<TBase, TObj, THolder, TType>,
        TType extends ComponentType<TBase, ? extends TBase, TObj, THolder, TType, ?>,
        TBuilder
> {
    Supplier<Object> DUMMY_BUILDER = Suppliers.memoize(Object::new);

    ResourceLocation registryName();

    @ApiStatus.Internal
    TComponent newInstance(THolder holder, Consumer<TBuilder> builder);
}
