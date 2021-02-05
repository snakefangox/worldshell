package net.snakefangox.worldshell.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

public class WorldShellRenderCache {

	private static final List<RenderLayer> renderLayers = RenderLayer.getBlockLayers();

	private final Map<RenderLayer, VertexBuffer> bufferStorage = new HashMap<>();
	private final Map<RenderLayer, BufferBuilder> buffers = new HashMap<>();
	private final Set<RenderLayer> bufferFilled = new HashSet<>();
	private final VertexFormat vertexFormat = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;

	public WorldShellRenderCache() {
		fillBuffers();
	}

	public BufferBuilder get(RenderLayer renderLayer) {
		if (buffers.containsKey(renderLayer)) {
			bufferFilled.add(renderLayer);
			return buffers.get(renderLayer);
		} else {
			bufferFilled.add(getDefault());
			return buffers.get(getDefault());
		}
	}

	public void upload() {
		buffers.forEach((key, entry) -> {
			entry.end();
			bufferStorage.get(key).upload(entry);
		});
	}

	public void draw(MatrixStack matrices) {
		renderLayers.forEach((key) -> {
			if (bufferFilled.contains(key)) {
				VertexBuffer entry = bufferStorage.get(key);
				key.startDrawing();
				entry.bind();
				vertexFormat.startDrawing(0L);
				entry.draw(matrices.peek().getModel());
				VertexBuffer.unbind();
				vertexFormat.endDrawing();
				key.endDrawing();
			}
		});
	}

	public void reset() {
		buffers.forEach((key, entry) -> entry.reset());
		bufferStorage.forEach((key, entry) -> entry.close());
		buffers.clear();
		bufferStorage.clear();
		fillBuffers();
	}

	private void fillBuffers() {
		renderLayers.forEach(renderLayer -> {
			BufferBuilder bufferBuilder = new BufferBuilder(renderLayer.getExpectedBufferSize());
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
			buffers.put(renderLayer, bufferBuilder);
			bufferStorage.put(renderLayer, new VertexBuffer());
		});
	}

	private RenderLayer getDefault() {
		return RenderLayer.getSolid();
	}
}
